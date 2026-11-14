package org.talkdesk.conversation.account.impl.ui.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import org.talkdesk.conversation.account.api.AccountRoute

/**
 * The Account feature's navigation entries. The app adds them to its entry provider, which the home
 * shell uses to show the Account tab. [onSignOut] ends the session once the agent confirms log out.
 */
fun EntryProviderScope<NavKey>.accountEntries(onSignOut: () -> Unit) {
    entry<AccountRoute> { AccountFlow(onSignOut = onSignOut) }
}
