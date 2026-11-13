# Conversation — KMP app (shared logic, native UI per platform)

- Kotlin Multiplatform for everything **below the UI**: domain, data, persistence, networking, DI and MVI ViewModels are shared in `commonMain`. Package root: `org.talkdesk.conversation`.
- **UI is native per platform — never shared:**
  - Android: **Jetpack Compose** (androidx, Compose BOM) in `androidMain` of each feature's `<name>/impl` and in Android-only modules (`core:designsystem`, `core:navigation`, `androidApp`). No Compose Multiplatform, and never Compose in `commonMain`/`iosMain`.
  - iOS: **SwiftUI** in `iosApp/iosApp/` (`<Name>/`, `DesignSystem/`, `Navigation/`), consuming the shared ViewModels through the `ConversationKit` framework (SKIE for Flow/sealed/suspend interop).
- Target layout: `core/*` + one top-level folder per feature (`<name>/api|impl`, e.g. `auth/`), shared composition root + iOS umbrella framework in `:conversation-kit`, Android composition root in `androidApp`, convention plugins in `build-logic`.
- **Current state:** `build-logic` exists; `:conversation-kit` builds the `ConversationKit` framework that iOS imports (SKIE enabled); the old `sharedLogic` template module is gone. `core:designsystem`, `core:ui`, `core:network`, `core:datastore`, `conversation-kit` and `auth/` exist; other `core/*` modules do not exist yet. Never assume a class, task or module exists: look first. If something is missing, say so and propose it; don't silently create it.
- **Single environment**: no STG/QA/PROD, flavors or environment enum. Base URL from build config; logging only in debug builds.
- Presentation is **MVI** by convention, not by base class: each ViewModel owns its data-class `State` (one `StateFlow`), `onIntent(Intent)` and `Effect` flow, and decides which of those it needs. There is no shared `MviViewModel`. Each platform renders `State` and sends `Intent`s. **Never use Circuit.**
- Navigation: routes are `@Serializable` `NavKey` data in `<name>:api` (shared). Android: Navigation 3 `NavDisplay`. iOS: SwiftUI `NavigationStack` driven by the same routes.
- DI: Koin. Network: Ktor (`core:network`). Persistence: Room (`core:database`) + DataStore (`core:datastore`: settings and the auth session; tokens are stored unencrypted by decision, pending a security review). No SQLDelight.
- Dependency rule: `ui → domain ← data`; domain is pure Kotlin. No `impl → impl` between features.
- **Offline-first**: the local DB is the single source of truth. UI reads only `Flow`s from it; API calls only write into it. No cache policies.
- Errors: `resultCatching`/`dataCatching`, never `runCatching`/`mapCatching`. Typed `AppError`; user text per platform (Android string resources, iOS String Catalog).
- In KMP modules everything lives in `commonMain`; `androidMain`/`iosMain` hold only thin expect/actual and platform glue.
- Versions only in `gradle/libs.versions.toml`. Build config lives in `build-logic` convention plugins (`conversation.*`); module build files hold only plugin aliases + dependencies.
- **STRICT RULE: no tests.** This is a hackathon project. Never write, add or keep tests of any kind (unit, instrumented, UI, screenshot, Koin/graph checks) or test-only dependencies, source sets (`commonTest`, `androidHostTest`, `androidTest`, `iosTest`) or fakes. Verify by building only. If asked to add tests, remind the user of this rule first.
- Verify: `./gradlew check` (guards), `./gradlew :androidApp:assembleDebug`, and for iOS `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -destination 'generic/platform=iOS Simulator' build`.

## Design system
- **Source of truth:** the "Talkdesk Agent Mobile" design system (Cobalt palette): https://claude.ai/artifact/GaXk1DBHBDar5w1R7Yn6P7. Read it (Artifact tool, `read`, starting with `project/README.md`, then `project/tokens.json` and `project/components/<Name>/README.md`) before any UI work. Never guess its values.
- **Implementation:** Android `core:designsystem` (`TalkdeskTheme`, `TalkdeskIcons`, `component/Talkdesk*`); iOS `iosApp/iosApp/DesignSystem/` (`.talkdeskTheme()`, `TalkdeskIcons`, `Components/Talkdesk*`). Both use the same token names.
- Every screen renders inside `TalkdeskTheme` (Android) / `.talkdeskTheme()` (iOS). Style UI only with theme tokens (`TalkdeskTheme.colors/typography/spacing/shapes/sizes`, `TalkdeskColors`, `TalkdeskTypography`…). No hard-coded colours, font sizes or dimensions, and no `cobalt-*` primitives.
- **Use the existing Talkdesk components** (buttons, text fields, search, cards, dialogs, toasts, banners, progress…) and `TalkdeskIcons`. Don't use a stock Material/SwiftUI control where a Talkdesk component exists.
- **Never create a custom component without the user's permission.** This covers one-off styled composables/views inside features. If no existing component fits, stop, say what's missing and ask.
- **Adding a component to the design system** (`core:designsystem` or `DesignSystem/`) needs both:
  1. the component exists in the design system linked above (follow its card exactly: sizes, states, tokens), and
  2. the user's explicit permission.
  If it isn't in the design system, don't add it; tell the user it needs to be designed there first.
- Colours (`TalkdeskColors`) and icons (`ic_td_*.xml` drawables, `TalkdeskIcons`, `Assets.xcassets/Icons`) are generated from the design system's `tokens.json` and icon set. Regenerate them rather than hand-editing values, and ask before changing tokens.
- Build each component on both platforms with light and dark previews.

## KMP architecture

### MVI state
- Android UI collects one-shot effects with `CollectEffects` from `core:ui` (lifecycle-aware via `repeatOnLifecycle`), never with a bare `LaunchedEffect { flow.collect {} }`. State uses `collectAsStateWithLifecycle`. Put shared Android UI helpers like this in `core:ui`.
- A ViewModel exposes its `State` as one `StateFlow`. `State` holds only data: no Compose or SwiftUI types, colors or string resources.

### What goes in each module
- `<name>/api` (KMP): `commonMain` only. Public interfaces, shared models and `NavKey` routes. Keep it small, with no implementations.
- `<name>/impl` (KMP):
  - `commonMain`: ViewModels, `State`/`Intent`/`Effect`, use cases, repository implementations.
  - `androidMain`: Android-only implementations of interfaces, and the feature's Jetpack Compose screens (`ui/`, string resources in `src/androidMain/res`). The screens render the feature's ViewModel `State` and send `Intent`s; no business logic.
  - `iosMain`: only when Kotlin code needs an iOS API. SwiftUI screens live in `iosApp/`, never in `iosMain`.
- There is no `<name>/android` module: a feature is `api` + `impl`.
- Only create source set folders that contain code.
- Compiled module names must be unique (many modules are named `api`/`impl`, which clashes in `.kotlin_module` on Android and in the klib `unique_name` on iOS). TODO: the convention plugins give each module a unique Android namespace but don't set the Kotlin module name or klib unique name. Flag this instead of adding a per-module workaround.

### Dependency rules
- `impl` may depend on its own `api`, other features' `api`, and `core` modules.
- Only `androidApp` and `:conversation-kit` depend on an `impl`. They wire implementations to interfaces.
- `impl/androidMain` may depend on Android-only `core` modules (`core:designsystem`, `core:ui`; both added by the `kmp.feature.impl` plugin).

### Where platform code goes
- Used by more than one feature: a `core` module (`androidMain` + `iosMain`).
- Small and dependency-free: expect/actual.
- Needed by only one feature's ViewModel: an interface in that feature's `impl/commonMain`, with implementations in its `androidMain`/`iosMain`. Move it to `core` as soon as a second feature needs it.
- Swift-only or much simpler in Swift (CallKit, PushKit, AVAudioSession, async APIs): declare a Kotlin interface, implement it in Swift inside `iosApp/`, and inject it at app startup. Prefer callbacks over `suspend` in interfaces that Swift implements.
- Needed only by the UI (haptics, share sheet, opening settings): put it directly in Compose or SwiftUI.

### Providing dependencies (Koin)
- A module provides as many of its own dependencies as it can. The host (`androidApp`, `:conversation-kit`) provides only what the module cannot know: build-time values (e.g. `BuildConfig` credentials) and Swift-implemented interfaces.
- Each module exposes its own Koin `Module` for its bindings (`authModule`, `networkAndroidModule`, `authAndroidModule`, ...). Platform bindings live in that module's `androidMain`/`iosMain`; shared bindings in `commonMain`. The host only lists these modules.
- Don't create a module's `HttpClient`, `DataStore`, signer, database or similar in the app. If the app currently builds one, move it to the module that owns it (use `androidContext()` for Android APIs instead of app `BuildConfig`).
- Document in the module's Koin module KDoc what the host must still provide.

### iOS umbrella framework
- iOS consumes exactly ONE Kotlin framework, `ConversationKit`, produced by `:conversation-kit` (`conversation-kit/`). Swift uses `import ConversationKit`. SKIE (0.10.16) gives Swift native `async`/`AsyncSequence` for `suspend`/`Flow`, and exhaustive `switch` over sealed types (`onEnum(of:)`).
- Never produce a framework per feature or per core module. That duplicates the Kotlin runtime and creates incompatible copies of the same types in Swift.
- `export()` only the modules whose classes Swift uses by name: ViewModels, `State`/`Intent`/`Effect`, models, and interfaces Swift implements. Every exported module must be an `api(...)` dependency. Everything else stays `implementation(...)`.
- When adding a feature, also add it to `:conversation-kit`'s dependencies, export it if Swift needs it, and register its ViewModels in the iOS wiring.
- iOS does not clear ViewModels automatically. Every SwiftUI screen owns its ViewModel through `ScopedViewModel<VM>` (`:conversation-kit`, `ConversationBootstrap.makeLoginViewModel()` is the example): keep it in an `ObservableObject` and call `clear()` in `deinit`, which cancels the ViewModel's coroutines.
- iOS composition root: `iOSApp.init()` calls `ConversationBootstrap.shared.start(es256Signer:credentials:)`, which starts Koin with the iOS modules (`networkIosModule`, `authIosModule`) plus the Swift-implemented `Es256Signer` (CryptoKit) and `OAuthCredentialsProvider`. iOS OAuth credentials come from the git-ignored `iosApp/Configuration/Secrets.xcconfig` -> `Info.plist`; generate it with `iosApp/scripts/generate-secrets.sh` (same `AGENT_*_PRODUCTION_*` properties as Android). Never commit it.

### Build logic
- Use the existing convention plugins (`build-logic/convention`) for every module:
  - KMP library (`core/*`, `:conversation-kit`): `conversation.kmp.library`
  - Feature api (includes `navigation3-runtime`): `conversation.kmp.feature.api`
  - Feature impl (includes Compose + Android resources in `androidMain`, its own `:<feature>:api` and `:core:designsystem`): `conversation.kmp.feature.impl`
  - Android-only library: `conversation.android.library` / `conversation.android.library.compose`
  - Android application (includes activity-compose, Navigation 3, lifecycle-compose-runtime, Koin): `conversation.android.application.compose` (builds on `conversation.android.application`)
- Don't re-declare in a module build file anything these plugins already add (own `api`, designsystem, Compose, Koin). Add shared dependencies to the plugin instead.
- `androidApp` wires its modules (`core:*`, `auth:*`) from `AndroidApplicationComposeConventionPlugin` (`AppModules`). When adding a feature or core module, add it there, not in `androidApp/build.gradle.kts`.
- Don't hand-write `kotlin {}` or `android {}` configuration that a convention plugin already provides.
- Don't create new convention plugins or change existing ones without asking. If no plugin fits, explain what's missing and propose a change.

### Choosing libraries and solutions
Go through these in order before writing a feature or picking a dependency:
1. Official Google/AndroidX/Jetpack libraries (ViewModel, Lifecycle, Room, DataStore, Navigation, Paging, ...) and JetBrains/Kotlin libraries (kotlinx.coroutines, kotlinx.serialization, kotlinx.datetime, Ktor, ...). Check the current docs for KMP support, because it changes often.
2. A mature, widely used community KMP library, only when no official option fits. Say why the official options don't work.
3. A custom implementation, only as a last resort. Explain why options 1 and 2 don't fit.
- Check `libs.versions.toml` for an existing equivalent first.
- Ask before adding any new dependency.

## Skills
Skills under `.claude/skills/`. Load only the one matching the task: `build-logic`.
