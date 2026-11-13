package org.talkdesk.conversation.auth.impl.domain.repository

/** Checks that the signed-in agent may use the app (first authenticated call after login). */
internal interface AgentAccessChecker {
    suspend fun verify(): Result<Unit>
}