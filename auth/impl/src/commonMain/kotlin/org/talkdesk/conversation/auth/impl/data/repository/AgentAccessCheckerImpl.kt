package org.talkdesk.conversation.auth.impl.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import org.talkdesk.conversation.auth.impl.AuthConfig
import org.talkdesk.conversation.auth.impl.data.local.AuthStorage
import org.talkdesk.conversation.auth.impl.domain.repository.AgentAccessChecker
import org.talkdesk.conversation.auth.impl.domain.utils.AuthError
import org.talkdesk.conversation.auth.impl.domain.utils.authCatching
import org.talkdesk.conversation.core.network.utils.ApiException
import org.talkdesk.conversation.core.network.utils.apiCall
import org.talkdesk.conversation.core.network.utils.ensureSuccess

/** `GET /callbar/mobile-app/agents/me` through the authenticated client. A 2xx means whitelisted. */
internal class AgentAccessCheckerImpl(
    private val authorizedClient: HttpClient,
    private val storage: AuthStorage,
) : AgentAccessChecker {
    override suspend fun verify(): Result<Unit> = authCatching {
        val gateway = storage.read().regional?.gateway ?: throw AuthError.SignInFailed()
        try {
            apiCall {
                authorizedClient.get("${AuthConfig.PROTOCOL}$gateway${AuthConfig.AGENT_ME_PATH}")
                    .ensureSuccess()
            }
        } catch (e: ApiException) {
            throw when (e.error?.code) {
                AuthConfig.ERROR_NOT_WHITELISTED -> AuthError.NotWhitelisted
                AuthConfig.ERROR_MISSING_SCOPES -> AuthError.MissingScopes
                else -> e
            }
        }
    }
}