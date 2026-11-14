package org.talkdesk.conversation.auth.impl.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.talkdesk.conversation.auth.impl.domain.repository.AuthRepository

/**
 * Restores the stored session as soon as the process starts, with or without a screen. A push can start the
 * process with no UI (an incoming call), and features that react to the session must not wait for the login
 * screen to be shown. Restoring twice is harmless: the login screen still restores on its own.
 */
internal class SessionStartup(repository: AuthRepository) {
    init {
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch { repository.restore() }
    }
}
