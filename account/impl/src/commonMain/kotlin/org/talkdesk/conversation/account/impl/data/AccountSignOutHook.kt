package org.talkdesk.conversation.account.impl.data

import org.talkdesk.conversation.account.impl.domain.AccountRepository
import org.talkdesk.conversation.auth.api.SignOutHook

/** Sets the agent offline while the session is still valid, so no work is routed to a logged-out device. */
internal class AccountSignOutHook(private val repository: AccountRepository) : SignOutHook {
    override suspend fun beforeSignOut() {
        repository.markOffline(reason = "Mobile App: user logged out")
    }
}
