package org.talkdesk.conversation.account.impl.domain

import kotlinx.coroutines.flow.Flow
import org.talkdesk.conversation.account.api.AgentRepository
import org.talkdesk.conversation.account.impl.domain.model.AccountSnapshot

internal interface AccountRepository : AgentRepository {
    /** The cached profile and statuses; null until loaded. */
    fun observe(): Flow<AccountSnapshot?>

    /** Asks the server to move the agent to [statusId] and caches it on success. */
    suspend fun changeStatus(statusId: String): Result<Unit>

    /** Stores a status reported by the server (realtime or polling). */
    suspend fun applyRemoteStatus(statusId: String)

    suspend fun markOffline(reason: String): Result<Unit>

    /** Forgets everything cached about the agent (sign-out). */
    suspend fun clear()
}
