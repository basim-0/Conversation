package org.talkdesk.conversation.account.impl.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.talkdesk.conversation.account.impl.data.remote.AccountApi
import org.talkdesk.conversation.account.impl.domain.AgentStatusGateway

/**
 * Status source without a realtime channel: reads the status once each time the flow is collected.
 * Used on platforms that have no Osiris adapter yet.
 */
internal class RestAgentStatusGateway(private val api: AccountApi) : AgentStatusGateway {
    override fun statusUpdates(agentId: String): Flow<String> = flow {
        api.currentStatus(agentId)?.let { emit(it) }
    }
}
