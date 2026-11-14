package org.talkdesk.conversation.activity.impl.ui.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import org.talkdesk.conversation.activity.api.ActivityRoute

/**
 * The Activity feature's navigation entries. The app adds them to its entry provider, which the
 * home shell uses to show the Activity tab. [onNewCall] opens the dialer (a separate feature the app navigates to).
 */
fun EntryProviderScope<NavKey>.activityEntries(onNewCall: () -> Unit) {
    entry<ActivityRoute> { ActivityFlow(onNewCall = onNewCall) }
}
