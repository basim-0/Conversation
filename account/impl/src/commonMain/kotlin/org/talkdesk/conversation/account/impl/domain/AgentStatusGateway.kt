package org.talkdesk.conversation.account.impl.domain

import kotlinx.coroutines.flow.Flow

/**
 * Source of the agent's current status. Android uses the Osiris realtime channel; platforms without it
 * poll the REST endpoint.
 */
internal interface AgentStatusGateway {
    /**
     * Emits the agent's current status id now and every time it changes while collected.
     * Fails when the connection cannot be established or is lost: the collector retries.
     */
    fun statusUpdates(agentId: String): Flow<String>
}
