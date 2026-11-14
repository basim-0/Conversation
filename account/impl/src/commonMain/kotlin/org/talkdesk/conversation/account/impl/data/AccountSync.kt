package org.talkdesk.conversation.account.impl.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.launch
import org.talkdesk.conversation.account.impl.domain.AccountRepository
import org.talkdesk.conversation.account.impl.domain.AgentStatusGateway
import org.talkdesk.conversation.auth.api.AuthSession
import org.talkdesk.conversation.auth.api.SessionState

/**
 * Keeps the cached profile in step with the session: loads it after sign-in, follows the live status
 * while signed in, and forgets everything after sign-out.
 */
internal class AccountSync(
    private val session: AuthSession,
    private val repository: AccountRepository,
    private val gateway: AgentStatusGateway,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        scope.launch {
            session.state
                .map { it is SessionState.SignedIn }
                .distinctUntilChanged()
                .collectLatest { signedIn -> if (signedIn) runSignedIn() }
        }
        scope.launch {
            session.state.filterIsInstance<SessionState.SignedOut>().collect { repository.clear() }
        }
    }

    private suspend fun runSignedIn() {
        val agentId = loadAgentId()
        gateway.statusUpdates(agentId)
            .retryWhen { cause, _ ->
                if (cause is CancellationException) false else { delay(RETRY_DELAY_MILLIS); true }
            }
            .collect { repository.applyRemoteStatus(it) }
    }

    /** The profile is needed for everything else: retry until it loads. */
    private suspend fun loadAgentId(): String {
        while (true) {
            repository.refresh().onSuccess { return repository.agentId() }
            delay(RETRY_DELAY_MILLIS)
        }
    }

    private companion object {
        const val RETRY_DELAY_MILLIS = 5_000L
    }
}
