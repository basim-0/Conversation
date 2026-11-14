package org.talkdesk.conversation.account.impl.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.talkdesk.conversation.core.network.ApiBaseUrl
import org.talkdesk.conversation.core.network.utils.apiCall
import org.talkdesk.conversation.core.network.utils.ensureSuccess

/** Talkdesk API calls behind the Account tab, made with the authenticated client. */
internal class AccountApi(
    private val client: HttpClient,
    private val baseUrl: ApiBaseUrl,
) {
    suspend fun agentMe(): AgentMeDto = apiCall {
        client.get("${baseUrl.get()}/callbar/mobile-app/agents/me").ensureSuccess().body()
    }

    /** `PATCH callbar/mobile-app/agents/{agentId}/status/{statusId}`, empty body. */
    suspend fun changeStatus(agentId: String, statusId: String): Unit = apiCall {
        client.patch("${baseUrl.get()}/callbar/mobile-app/agents/$agentId/status/$statusId").ensureSuccess()
        Unit
    }

    /** Current status id of the agent, or null when the server does not know it. */
    suspend fun currentStatus(agentId: String): String? = apiCall {
        val rows: List<UserStatusDto> = client.post("${baseUrl.get()}/ccaas/users/status") {
            contentType(ContentType.Application.Json)
            setBody(UserStatusesRequest(listOf(agentId)))
        }.ensureSuccess().body()
        rows.firstOrNull { it.id == agentId }?.status
    }

    /** `PUT callbar/user/status`: presence set on behalf of the app (offline on logout, busy on dial). */
    suspend fun setUserStatus(status: String, reason: String): Unit = apiCall {
        client.put("${baseUrl.get()}/callbar/user/status") {
            contentType(ContentType.Application.Json)
            setBody(SetStatusRequest(status, reason))
        }.ensureSuccess()
        Unit
    }

    suspend fun heartbeat(): Unit = apiCall {
        client.put("${baseUrl.get()}/users/heartbeat") {
            contentType(ContentType.Application.Json)
            setBody(HeartbeatRequest(HEARTBEAT_DURATION_SECONDS))
        }.ensureSuccess()
        Unit
    }

    private companion object {
        /** One week, as the legacy app. */
        const val HEARTBEAT_DURATION_SECONDS = 604_800L
    }
}
