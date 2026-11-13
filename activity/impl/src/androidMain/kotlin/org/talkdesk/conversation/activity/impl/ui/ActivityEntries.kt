package org.talkdesk.conversation.activity.impl.ui

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import org.talkdesk.conversation.activity.api.ActivityRoute

/**
 * The Activity feature's navigation entries. The app adds them to its entry provider, which the
 * home shell uses to show the Activity tab.
 */
fun EntryProviderScope<NavKey>.activityEntries() {
    entry<ActivityRoute> { ActivityFlow() }
}
