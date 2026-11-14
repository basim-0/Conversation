package org.talkdesk.conversation.activity.impl.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import org.talkdesk.conversation.core.network.ApiBaseUrl
import org.talkdesk.conversation.core.network.utils.apiCall
import org.talkdesk.conversation.core.network.utils.ensureSuccess

/** Talkdesk API calls behind the Activity tab, made with the authenticated client. */
internal class ActivityApi(
    private val client: HttpClient,
    private val baseUrl: ApiBaseUrl,
) {
    /**
     * One page of the history, newest first. [dateFrom] and [dateTo] are API timestamps
     * (`yyyy-MM-dd'T'HH:mm:ss.SSS'Z'`, UTC).
     */
    suspend fun interactionContacts(
        page: Int,
        types: List<String>,
        dateFrom: String,
        dateTo: String,
    ): InteractionContactsDto = apiCall {
        client.get("${baseUrl.get()}/interaction-contacts") {
            parameter("page", page)
            parameter("per_page", PAGE_SIZE)
            parameter("date__from", dateFrom)
            parameter("date__to", dateTo)
            parameter("order_by", ORDER_BY)
            EXTERNAL_INTEGRATIONS.forEach { parameter("external_integrations", it) }
            types.forEach { parameter("type", it) }
        }.ensureSuccess().body()
    }

    suspend fun contacts(ids: List<String>): ContactsDto = apiCall {
        client.get("${baseUrl.get()}/contacts") {
            ids.forEach { parameter("ids[]", it) }
            parameter("include_deleted", true)
        }.ensureSuccess().body()
    }

    companion object {
        const val PAGE_SIZE = 20
        private const val ORDER_BY = "interaction_started_at:DESC"
        private val EXTERNAL_INTEGRATIONS = listOf("SMS", "EMAIL", "WHATSAPP")
    }
}
