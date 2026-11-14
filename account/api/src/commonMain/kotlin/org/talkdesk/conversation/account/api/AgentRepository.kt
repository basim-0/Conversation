package org.talkdesk.conversation.account.api

import kotlinx.coroutines.flow.Flow

/** The signed-in agent, as returned by `agents/me`. */
data class Agent(
    val id: String,
    val accountId: String?,
    val name: String,
    val role: String?,
    val permissions: Set<String>,
)

/**
 * The signed-in agent's profile and presence, for features that need to act as the agent
 * (calling, activity...). Backed by the local database; the Account feature owns and refreshes it.
 */
interface AgentRepository {
    /** The cached profile; null until it is loaded for the first time or after sign-out. */
    val agent: Flow<Agent?>

    /** The agent id, loading the profile first when it is not cached yet. Throws when it cannot be loaded. */
    suspend fun agentId(): String

    /** Fetches the profile again from the server. */
    suspend fun refresh(): Result<Unit>

    /** Tells the server the agent is active (`PUT users/heartbeat`). Best effort. */
    suspend fun heartbeat()

    /** Sets the agent's presence to busy, e.g. while placing an outbound call. */
    suspend fun markBusy(reason: String): Result<Unit>
}
