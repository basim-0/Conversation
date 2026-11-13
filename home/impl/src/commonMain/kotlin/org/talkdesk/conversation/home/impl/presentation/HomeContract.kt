package org.talkdesk.conversation.home.impl.presentation

import androidx.navigation3.runtime.NavKey
import org.talkdesk.conversation.activity.api.ActivityRoute
import org.talkdesk.conversation.home.api.AccountRoute
import org.talkdesk.conversation.home.api.ContactsRoute
import org.talkdesk.conversation.home.api.ConversationsRoute
import org.talkdesk.conversation.home.api.VoicemailsRoute

/**
 * Top-level destinations of the home bottom navigation, in display order. Each tab is a navigation
 * route; the screen behind it comes from its feature through the app's entry provider.
 */
enum class HomeTab(val route: NavKey) {
    Activity(ActivityRoute),
    Contacts(ContactsRoute),
    Conversations(ConversationsRoute),
    Voicemails(VoicemailsRoute),
    Account(AccountRoute),
    ;

    companion object {
        fun fromRoute(route: NavKey): HomeTab = entries.first { it.route == route }
    }
}
