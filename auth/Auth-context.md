# Auth — context

The Conversation app uses the same login flow as the Talkdesk Agent Android app (`agent-mobile-android`). This file records how that flow works and the decisions made for this project. Read it before working on `auth/`.

## What it is

Login uses **Talkdesk ID**: OAuth 2.0 / OpenID Connect with an authorization code + PKCE. It is **not Auth0**. The client authenticates on token calls with a **signed JWT (private_key_jwt, ES256)**, not a client secret.

## Decisions

| Topic | Decision |
|---|---|
| Environment | Production only |
| OAuth clients | Reuse the Agent app's clients: same client IDs, keys and redirect URI (same product) |
| Private key embedded in app | Kept as in the old app for now. Decision deferred, needs a security review before release. Keep signing behind one interface so it can be swapped |
| Refresh trigger | Error code `0011011` in the response body, regardless of HTTP status (same as old app) |
| Scopes | None requested, so the server's default scopes for the client apply (same as old app) |
| Phase 1 | Account screen, region lookup, browser login, code exchange, authenticated calls with refresh-on-error, logout, whitelist and missing-scope handling |
| Phase 2 | Periodic background token refresh (WorkManager on Android, BGTask on iOS) |
| Token storage | Plain (unencrypted) DataStore via `core:datastore`, behind the `AuthStorage` interface in `auth/impl`. Decided for speed; needs a security review before release (Android Keystore / iOS Keychain are the alternatives). The Android manifest sets `allowBackup=false` so tokens are not copied to cloud backups |

## Configuration (production)

| Name | Value |
|---|---|
| Protocol | `https://` |
| Host suffix | `.talkdeskid.com` |
| Authorize path | `/oauth/authorize` |
| Token path | `/oauth/token` |
| End-session path | `/oauth/end_session` |
| Redirect URI | `com.talkdesk.agent://auth` (Android scheme `com.talkdesk.agent`) |
| JWT assertion lifetime | 840 s |
| Refresh max retry attempts | 3 |
| Background refresh period (phase 2) | 240 min |

The per-region credentials are secrets. They live in `~/.gradle/gradle.properties` and must **never be committed**. Each region has a client ID, a private key (base64 PKCS#8, EC) and a key ID:

| Region | Properties |
|---|---|
| US (default) | `AGENT_PRODUCTION_CLIENT_ID`, `AGENT_PRODUCTION_PRIVATE_KEY`, `AGENT_PRODUCTION_CLIENT_KEY_ID` |
| CA | `AGENT_CA_PRODUCTION_CLIENT_ID`, `AGENT_CA_PRODUCTION_PRIVATE_KEY`, `AGENT_CA_PRODUCTION_CLIENT_KEY_ID` |
| EU | `AGENT_EU_PRODUCTION_*` (same three) |
| AU | `AGENT_AU_PRODUCTION_*` |
| UK | `AGENT_UK_PRODUCTION_*` |

## Flow

### 1. Account name
The user enters their Talkdesk account name. Lowercase it. It is valid if it matches `^[a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?$`. Otherwise show an "invalid account" error.

### 2. Region lookup (no auth)
```
GET https://account.talkdeskid.com/accounts/{account}/entrypoint
→ { "target_issuer": "...", "td_region": "...", "target_gateway": "..." }
```
- `target_issuer` is the OAuth host. All OAuth URLs are `https://{target_issuer}{path}`.
- `target_gateway` is the API host. The API base URL is `https://{target_gateway}/`.
- `td_region` selects the credentials. Check whether it contains `td-ca`, `td-eu`, `td-ap` (→ AU) or `td-uk`. If none match, or the region is unknown, use US.

Clear any previous regional config before each new lookup.

### 3. Authorize (browser)
Open `https://{issuer}/oauth/authorize` with:
- `response_type=code`
- `client_id={regional client id}`
- `redirect_uri=com.talkdesk.agent://auth`
- PKCE `code_challenge` (S256) + `code_challenge_method`
- `state`

No `scope` parameter. The old app used AppAuth in a Chrome Custom Tab. If the browser is cancelled or closed, cancel the login and return to the account screen.

### 4. Code → token
```
POST https://{issuer}/oauth/token   (form-urlencoded)
grant_type=authorization_code
code=…
redirect_uri=com.talkdesk.agent://auth
code_verifier=…
client_id=…
client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer
client_assertion={signed JWT}
→ { access_token, refresh_token, scope, … }
```
Store the access token, refresh token, scope, issuer, account name and an update timestamp.

### 5. Client assertion JWT (ES256)
- Header: `kid` = regional key ID
- Claims:
  - `jti` = random UUID
  - `iss` = `sub` = client ID
  - `aud` = the URL being called (token URL or end-session URL)
  - `iat` = now (seconds)
  - `exp` = now + 840
- Signed with the regional EC private key.

### 6. Authenticated API calls
- Headers:
  - `Authorization: Bearer {access_token}`
  - `User-agent: {appName}/{appVersion} {http.agent system property}`
- First call after login: `GET https://{gateway}/callbar/mobile-app/agents/me`.
- A successful response marks the account as whitelisted.

Error codes come from the response body's error `code`:

| Code | Meaning | Action |
|---|---|---|
| `0011011` | Access token expired | Refresh (step 7), then retry the request once |
| `0410010` | Account not whitelisted | Show the "not whitelisted" screen |
| `0011016` | Missing scopes | Revoke the session and send the user back to login |

### 7. Refresh
```
POST https://{issuer}/oauth/token   (form-urlencoded)
grant_type=refresh_token
refresh_token=…
scope={stored scope}
client_assertion_type=…jwt-bearer
client_assertion={JWT, aud = token URL}
```
- Only one refresh runs at a time. Concurrent callers wait for it and then use the new token.
- On failure, count an attempt. Network-unreachable errors don't count.
- Revoke the session if the refresh token is expired (`0011011`) or after 3 failed attempts. Revoking marks the refresh token as expired and sends the user back to the account screen.

### 8. Logout
1. Put the agent offline. This is a separate API, so wire it later if needed.
2. Call end session:
   ```
   POST https://{issuer}/oauth/end_session   (form-urlencoded)
   client_id=…
   refresh_token=…
   client_assertion_type=…jwt-bearer
   client_assertion={JWT, aud = end-session URL}
   ```
3. Clear the tokens and the whitelist status, and cancel background refresh (phase 2).
4. Return to the account screen.

On session loss, do the same steps best-effort and ignore errors.

## App start
- If stored auth has a valid token, the user is signed in.
- If the stored issuer is missing, re-run the region lookup for the stored account.
- If the refresh token is marked expired, go to the account screen with the account name pre-filled and restart authorization.

## Planned implementation in this project
- **Shared code (`commonMain`, Ktor):**
  - region lookup and credential choice
  - authorize URL, PKCE and `state`
  - code exchange, refresh and logout
  - JWT assertion
  - error-code handling, mapped to typed `AppError`
- **Per platform:** only the browser step.
  - The ViewModel emits an `OpenBrowser(url)` effect.
  - Android uses Auth Tab with a Custom Tab fallback. Auth Tab avoids redirect clashes with the Agent app, which uses the same scheme.
  - iOS uses `ASWebAuthenticationSession`.
  - Each platform sends the redirect URI back as an intent.
- **Modules:**
  - `auth/api`: routes and the `AuthSession` interface (signed-in state Flow, `logout()`).
  - `auth/impl`: ViewModel, `State`/`Intent`/`Effect`, use cases and services.
  - Compose screens: `auth/impl/src/androidMain` (`ui/login`).
  - `iosApp/iosApp/Auth/`: SwiftUI screens.
- **`core:network`:** Ktor client, bearer header and the refresh-on-`0011011` interceptor (single-flight).
- **ES256 signing:** platform code, Java security on Android and CryptoKit on iOS (implemented in Swift). No new crypto library.
- **Dependencies (approved):** Ktor client, `androidx.browser` 1.10, `androidx.datastore` 1.3.

## Implementation status (Android)
- `core:datastore`: `createDataStore`, per-platform file location. `core:network`: `createHttpClient`, `ApiException`/`apiCall`, `withBearerRefresh` (single-flight refresh + one retry).
- `auth/api`: `LoginRoute`, `AuthSession`, `SessionState`. `auth/impl`: domain, data, `LoginViewModel` (plain androidx `ViewModel`, no shared base class), `authModule`, `JavaEs256Signer`.
- `auth/impl/androidMain/ui/login`: `LoginFlow` (splash, screens, Auth Tab with Custom Tab fallback). There is no `auth/android` module. `androidApp`: Application + Koin, credentials from BuildConfig, root `NavDisplay`.
- **iOS** (same shared logic): `:conversation-kit` builds the `ConversationKit` framework (SKIE). `ConversationBootstrap.start(...)` starts Koin (`networkIosModule`, `authIosModule`, `authModule`). Swift provides `CryptoKitEs256Signer` (raw R||S, verified against the production keys) and `BundleOAuthCredentials` (from `Info.plist`, filled by `Secrets.xcconfig`). `WebAuthenticator` wraps `ASWebAuthenticationSession`; redirect -> `RedirectReceived`, cancel -> `BrowserClosed`. `LoginFlowView` mirrors Android's `LoginFlow`; `ConversationRoot` follows `AuthSession.state`. Setup: run `iosApp/scripts/generate-secrets.sh` once (AU/UK stay empty like Android).
- The host app provides to `authModule`: base `HttpClient`, `DataStore<Preferences>` qualified `AuthDataStore`, `OAuthCredentialsProvider`, `Es256Signer`.
- Temporary: a sign-out button as the signed-in screen; "not whitelisted" shows the generic sign-in-failed banner.
- Errors use a feature-local `AuthError` because `AppError`/`resultCatching` do not exist yet.
- The pending PKCE verifier/state live in memory only: if the process dies while the browser is open, the login must be restarted.

## Old project reference
`~/StudioProjects/agent-mobile-android/app/src/main/kotlin/com/talkdesk/agent/`:
- `authorization/TalkdeskAuthorizationManager.kt`: state machine for start, finish, refresh and revoke
- `authorization/CompanyAccountPresenter.kt`: account screen logic
- `authorization/openid/`: AppAuth intent and token exchange
- `authorization/http/HttpTokenService.kt`: refresh and end-session
- `authorization/http/AuthorizationInterceptor.kt`: bearer header and refresh-on-error
- `authorization/SignedJwtProvider.kt`, `JwtBearerAssertionParameters.kt`: client assertion
- `authorization/TalkdeskOAuthClientCredentialsProvider.kt`: region to credentials
- `regionalconfiguration/`: entrypoint lookup
- `session/TalkdeskSessionManager.kt`: logout and session loss
- `state/ServiceException.kt`: error codes
- Config: `gradle.properties` (non-secret), `~/.gradle/gradle.properties` (secrets)
