package org.talkdesk.conversation.core.push

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.talkdesk.conversation.core.network.ApiBaseUrl
import org.talkdesk.conversation.core.network.utils.apiCall
import org.talkdesk.conversation.core.network.utils.ensureSuccess

/** Registers this device with Talkdesk so the backend can push to it (voicemail, ACW...). */
class DeviceRegistrationApi(
    private val client: HttpClient,
    private val baseUrl: ApiBaseUrl,
    private val platform: PushPlatform,
) {
    /** `POST users/{agentId}/devices`. Idempotent: safe to repeat on every start and token change. */
    suspend fun register(agentId: String, token: String): Unit = apiCall {
        client.post("${baseUrl.get()}/users/$agentId/devices") {
            contentType(ContentType.Application.Json)
            setBody(
                buildJsonObject {
                    put("push_information", buildJsonObject {
                        put("push_notification_service", platform.serviceName)
                        platform.bundle?.let { put("push_notification_bundle", it) }
                        put("push_notification_token", token)
                        put("call_information_push", false)
                    })
                }
            )
        }.ensureSuccess()
        Unit
    }
}
