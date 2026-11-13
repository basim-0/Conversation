package org.talkdesk.conversation.core.network.plugins

import io.ktor.client.HttpClient
import io.ktor.client.call.save
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.talkdesk.conversation.core.network.utils.ApiError
import org.talkdesk.conversation.core.network.utils.parseApiError

/** Hooks that the owning feature provides to [withBearerRefresh]. */
class BearerRefreshConfig {
    /** Returns the current access token, or null when signed out. */
    var currentToken: suspend () -> String? = { null }

    /**
     * Obtains a new access token. Called at most once at a time (single-flight); callers that were
     * waiting reuse the token produced by the call that ran. Return null when refresh is impossible.
     */
    var refreshToken: suspend (staleToken: String?) -> String? = { null }

    /** True when [error] means the access token expired and a refresh + one retry should happen. */
    var shouldRefresh: (ApiError) -> Boolean = { false }

    /** Called with every error body of a failed response (for feature-specific reactions). */
    var onError: suspend (ApiError) -> Unit = {}
}

private val BearerRefreshPlugin = createClientPlugin("BearerRefresh", ::BearerRefreshConfig) {

    val refreshLock = Mutex()
    suspend fun freshTokenFor(sentToken: String?): String? = refreshLock.withLock {
        val current = pluginConfig.currentToken()
        val alreadyRefreshed = current != null && current != sentToken
        if (alreadyRefreshed) current else pluginConfig.refreshToken(sentToken)
    }

    on(Send) { request ->
        // 1. First attempt, with the current token if signed in.
        val sentToken = pluginConfig.currentToken()
        sentToken?.let { request.header(HttpHeaders.Authorization, "Bearer $it") }
        val call = proceed(request).save()
        if (call.response.status.isSuccess()) return@on call

        // 2. Failed: report the error and decide whether an expired token is the cause.
        val error = parseApiError(call.response.bodyAsText())
        error?.let { pluginConfig.onError(it) }
        val tokenExpired = error != null && pluginConfig.shouldRefresh(error)
        if (!tokenExpired) return@on call

        // 3. Refresh once and retry once; give up (return the original failure) if refresh is impossible.
        val freshToken = freshTokenFor(sentToken) ?: return@on call
        request.headers.remove(HttpHeaders.Authorization)
        request.header(HttpHeaders.Authorization, "Bearer $freshToken")
        proceed(request).save()
    }
}

/**
 * Returns a client that adds `Authorization: Bearer`, and on an expired-token error refreshes the
 * token once (single-flight) and retries the request once. Use it for authenticated API calls only.
 */
fun HttpClient.withBearerRefresh(configure: BearerRefreshConfig.() -> Unit): HttpClient =
    config { install(BearerRefreshPlugin, configure) }
