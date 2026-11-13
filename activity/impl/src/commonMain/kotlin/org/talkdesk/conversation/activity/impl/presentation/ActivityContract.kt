package org.talkdesk.conversation.activity.impl.presentation

/**
 * Data contract of the Activity tab. UI-only for now: a ViewModel will later own an [ActivityState]
 * in one `StateFlow`, receive [ActivityIntent]s through `onIntent`, and apply the filters itself,
 * so [ActivityContent.Loaded] already holds the rows that are visible.
 */
data class ActivityState(
    val selectedTab: ActivityTab = ActivityTab.All,
    /** Unread missed calls, shown as a badge on the Missed tab. */
    val missedCount: Int = 0,
    /** Channel chips are multi-select; empty means no filter. Only used on the All tab. */
    val selectedChannels: Set<ActivityChannel> = emptySet(),
    val content: ActivityContent = ActivityContent.Loading,
    val isOffline: Boolean = false,
    /** Time of the cached data, shown in the offline banner. */
    val lastUpdatedLabel: String? = null,
    val isRefreshing: Boolean = false,
)

enum class ActivityTab { All, Missed }

enum class ActivityChannel { Calls, Sms, WhatsApp, Email }

sealed interface ActivityContent {
    /** First load only: skeleton rows. Later refreshes keep the list. */
    data object Loading : ActivityContent

    /** Rows grouped by day. An empty list is the empty state. */
    data class Loaded(val sections: List<ActivitySection>) : ActivityContent

    /** First load failed and there is no cache to show. */
    data object Failed : ActivityContent
}

data class ActivitySection(val day: ActivityDay, val items: List<ActivityItem>)

sealed interface ActivityDay {
    data object Today : ActivityDay
    data object Yesterday : ActivityDay
    data class Date(val label: String) : ActivityDay
}

/**
 * One row. The sub-line is "<kind label> · [detail]" (e.g. "Missed · Support line"), so [detail]
 * is the line name, call duration or message preview.
 */
data class ActivityItem(
    val id: String,
    val kind: ActivityKind,
    val title: String,
    val detail: String,
    val timeLabel: String,
    val unreadCount: Int = 0,
)

sealed interface ActivityKind {
    data class Call(val direction: CallDirection) : ActivityKind
    data class Message(val channel: MessageChannel) : ActivityKind
}

enum class CallDirection { Inbound, Outbound, Missed, Abandoned }

enum class MessageChannel { Sms, WhatsApp, Email }

sealed interface ActivityIntent {
    data class SelectTab(val tab: ActivityTab) : ActivityIntent
    data class ToggleChannel(val channel: ActivityChannel) : ActivityIntent
    data object ClearFilters : ActivityIntent
    data object Refresh : ActivityIntent
    data object Retry : ActivityIntent
    data class CallBack(val itemId: String) : ActivityIntent
    data class OpenItem(val itemId: String) : ActivityIntent
    data object NewCall : ActivityIntent
}
