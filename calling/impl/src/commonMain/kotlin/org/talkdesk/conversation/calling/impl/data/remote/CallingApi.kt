package org.talkdesk.conversation.calling.impl.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.talkdesk.conversation.calling.impl.domain.port.ClientInfo
import org.talkdesk.conversation.core.network.ApiBaseUrl
import org.talkdesk.conversation.core.network.utils.apiCall
import org.talkdesk.conversation.core.network.utils.ensureSuccess

/** Talkdesk API calls behind voice calling, made with the authenticated client. */
internal class CallingApi(
    private val client: HttpClient,
    private val baseUrl: ApiBaseUrl,
    private val clientInfo: ClientInfo,
) {
    /** A fresh Twilio access token. Short-lived: never cache it across calls. */
    suspend fun voiceToken(): String = apiCall {
        client.get("${baseUrl.get()}/callbar/mobile-app/providers/twilio").ensureSuccess()
            .body<VoiceTokenDto>().credentials.accessToken
    }

    suspend fun callInfo(providerCallId: String): CallInfoDto = apiCall {
        client.get("${baseUrl.get()}/callbar/mobile-app/calls-info/$providerCallId").ensureSuccess().body()
    }

    /** Hold is server-side: the bridge holds the customer. */
    suspend fun hold(interactionId: String): Unit = apiCall {
        client.post("${baseUrl.get()}/interactions/$interactionId/actions/hold") {
            contentType(ContentType.Application.Json)
            setBody(HoldRequest(shouldPlayHoldBeep = true))
        }.ensureSuccess()
        Unit
    }

    suspend fun unhold(interactionId: String): Unit = apiCall {
        client.post("${baseUrl.get()}/interactions/$interactionId/actions/unhold").ensureSuccess()
        Unit
    }

    /** Ends the call from the server side, which is how the legacy app hangs up a normal call. */
    suspend fun disconnect(providerCallId: String, interactionId: String?): Unit = apiCall {
        client.post("${baseUrl.get()}/callbar/mobile-app/interactions/disconnect/$providerCallId") {
            contentType(ContentType.Application.Json)
            setBody(DisconnectRequest(interactionId))
        }.ensureSuccess()
        Unit
    }

    /** Registers the call for Talkdesk analytics (Looker). */
    suspend fun registerClient(callSid: String): Unit = apiCall {
        client.post("${baseUrl.get()}/callbar/call/client/$callSid") {
            contentType(ContentType.Application.Json)
            setBody(
                buildJsonObject {
                    put("type", clientInfo.platform)
                    put("integration", JsonNull)
                    put("version", clientInfo.version)
                },
            )
        }.ensureSuccess()
        Unit
    }

    /** The caller ID the agent's calls go out with. */
    suspend fun selectedCallerId(): CallerIdDto? = apiCall {
        client.get("${baseUrl.get()}/callbar/mobile-app/outbound-caller-ids") {
            header("x-api-version", "v2")
            parameter("per_page", CALLER_ID_PAGE_SIZE)
            parameter("capability", "voice")
        }.ensureSuccess().body<CallerIdsDto>().selected
    }

    private companion object {
        const val CALLER_ID_PAGE_SIZE = 20
    }
}
