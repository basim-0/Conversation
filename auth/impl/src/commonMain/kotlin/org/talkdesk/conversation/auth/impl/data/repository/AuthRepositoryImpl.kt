package org.talkdesk.conversation.auth.impl.data.repository

import io.ktor.http.URLBuilder
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import org.talkdesk.conversation.auth.api.SessionState
import org.talkdesk.conversation.auth.impl.AuthConfig
import org.talkdesk.conversation.auth.impl.data.local.AuthStorage
import org.talkdesk.conversation.auth.impl.data.remote.TalkdeskIdApi
import org.talkdesk.conversation.auth.impl.domain.AccountName
import org.talkdesk.conversation.auth.impl.domain.utils.AuthError
import org.talkdesk.conversation.auth.impl.domain.repository.AuthRepository
import org.talkdesk.conversation.auth.impl.domain.model.AuthorizationRequest
import org.talkdesk.conversation.auth.impl.domain.OAuthCredentialsProvider
import org.talkdesk.conversation.auth.impl.domain.utils.Pkce
import org.talkdesk.conversation.auth.impl.domain.model.RegionalConfiguration
import org.talkdesk.conversation.auth.impl.domain.model.Tokens
import org.talkdesk.conversation.auth.impl.domain.utils.authCatching
import org.talkdesk.conversation.core.network.utils.ApiException
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

internal class AuthRepositoryImpl(
    private val api: TalkdeskIdApi,
    private val storage: AuthStorage,
    private val credentialsProvider: OAuthCredentialsProvider,
    private val nowMillis: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) : AuthRepository {

    private class Pending(val regional: RegionalConfiguration, val pkce: Pkce)

    private val _session = MutableStateFlow<SessionState>(SessionState.Loading)
    override val session: StateFlow<SessionState> = _session

    private val refreshLock = Mutex()
    private var refreshFailures = 0
    private var pending: Pending? = null

    override suspend fun restore() {
        val stored = storage.read()
        val account = stored.accountName
        _session.value = when {
            stored.tokens == null -> SessionState.SignedOut(account, sessionExpired = false)
            stored.refreshTokenExpired || account == null -> SessionState.SignedOut(account, sessionExpired = true)
            else -> {
                if (stored.regional == null) {
                    // Issuer missing: look it up again; failing here must not sign the user out.
                    authCatching { api.lookUpRegion(account) }.onSuccess { storage.saveRegional(it) }
                }
                SessionState.SignedIn(account)
            }
        }
    }

    override suspend fun beginAuthorization(rawAccountName: String): Result<AuthorizationRequest> =
        authCatching {
            val account = AccountName.parse(rawAccountName).getOrThrow()
            val regional = try {
                api.lookUpRegion(account)
            } catch (e: ApiException) {
                if (e.httpStatus == NOT_FOUND) throw AuthError.AccountNotFound else throw e
            }
            storage.saveAccount(account, regional)

            val credentials = credentialsProvider.forRegion(regional.region)
            val pkce = Pkce.generate()
            pending = Pending(regional, pkce)

            val url = URLBuilder(regional.url(AuthConfig.AUTHORIZE_PATH)).apply {
                parameters.append("response_type", "code")
                parameters.append("client_id", credentials.clientId)
                parameters.append("redirect_uri", AuthConfig.REDIRECT_URI)
                parameters.append("code_challenge", pkce.challenge)
                parameters.append("code_challenge_method", "S256")
                parameters.append("state", pkce.state)
            }.buildString()
            AuthorizationRequest(url)
        }

    override fun cancelAuthorization() {
        pending = null
    }

    override suspend fun completeAuthorization(redirectUri: String): Result<Unit> = authCatching {
        val started = pending ?: throw AuthError.StateMismatch
        val params = Url(redirectUri).parameters
        if (params["state"] != started.pkce.state) throw AuthError.StateMismatch
        val code = params["code"]
        if (params["error"] != null || code == null) throw AuthError.SignInFailed()

        val credentials = credentialsProvider.forRegion(started.regional.region)
        val token = api.exchangeCode(started.regional, credentials, code, started.pkce.verifier)
        val refreshToken = token.refreshToken ?: throw AuthError.SignInFailed()
        storage.saveTokens(
            Tokens(
                token.accessToken,
                refreshToken,
                token.scope.orEmpty(),
                nowMillis()
            )
        )
        pending = null
        refreshFailures = 0
    }

    override suspend fun markSignedIn() {
        storage.read().accountName?.let { _session.value = SessionState.SignedIn(it) }
    }

    override suspend fun currentAccessToken(): String? = storage.read().tokens?.accessToken

    override suspend fun refreshAccessToken(): String? = refreshLock.withLock {
        val stored = storage.read()
        val tokens = stored.tokens ?: return@withLock null
        val regional = stored.regional ?: return@withLock null
        val credentials = credentialsProvider.forRegion(regional.region)
        try {
            val response = api.refresh(regional, credentials, tokens.refreshToken, tokens.scope)
            val fresh = Tokens(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken ?: tokens.refreshToken,
                scope = response.scope ?: tokens.scope,
                updatedAtMillis = tokens.updatedAtMillis,
            )
            storage.saveTokens(fresh)
            refreshFailures = 0
            fresh.accessToken
        } catch (e: ApiException) {
            // Unreachable network does not count as a failed attempt.
            if (!e.isNetwork) {
                refreshFailures++
                val expired = e.error?.code == AuthConfig.ERROR_TOKEN_EXPIRED
                if (expired || refreshFailures >= AuthConfig.REFRESH_MAX_ATTEMPTS) revokeLocked()
            }
            null
        }
    }

    override suspend fun revoke() = refreshLock.withLock { revokeLocked() }

    // Session loss: same steps as logout, best effort, then mark the refresh token as expired.
    private suspend fun revokeLocked() {
        endSessionBestEffort()
        storage.markRefreshTokenExpired()
        refreshFailures = 0
        _session.value = SessionState.SignedOut(storage.read().accountName, sessionExpired = true)
    }

    override suspend fun logout() {
        endSessionBestEffort()
        storage.clearTokens()
        refreshFailures = 0
        _session.value = SessionState.SignedOut(storage.read().accountName, sessionExpired = false)
    }

    override suspend fun forgetAccount() {
        storage.clearAccount()
        _session.value = SessionState.SignedOut(accountName = null, sessionExpired = false)
    }

    private suspend fun endSessionBestEffort() {
        val stored = storage.read()
        val tokens = stored.tokens ?: return
        val regional = stored.regional ?: return
        withTimeoutOrNull(AuthConfig.BEST_EFFORT_TIMEOUT_MILLIS.milliseconds) {
            // Errors are ignored on purpose: the local session is cleared regardless.
            authCatching {
                api.endSession(
                    regional,
                    credentialsProvider.forRegion(regional.region),
                    tokens.refreshToken
                )
            }
        }
    }

    private companion object {
        const val NOT_FOUND = 404
    }
}
