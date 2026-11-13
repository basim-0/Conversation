package org.talkdesk.conversation.activity.impl.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import org.talkdesk.conversation.activity.impl.domain.repository.ActivityRepository
import org.talkdesk.conversation.auth.api.AuthSession
import org.talkdesk.conversation.auth.api.SessionState

/** Clears the cached activity as soon as the agent signs out, so the next account never sees it. */
internal class ActivitySessionCleaner(session: AuthSession, repository: ActivityRepository) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        scope.launch {
            session.state.filterIsInstance<SessionState.SignedOut>().collect { repository.clear() }
        }
    }
}
