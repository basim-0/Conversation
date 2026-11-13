package org.talkdesk.conversation.activity.impl.presentation

/**
 * Data contract of the Activity tab. [ActivityViewModel] owns an [ActivityState] in one `StateFlow` and
 * receives [ActivityIntent]s through `onIntent`. It applies the filters itself, so
 * [ActivityContent.Loaded] already holds the rows that are visible.
 */
data class ActivityState(
    val selectedTab: ActivityTab = ActivityTab.All,
    /** Unread missed calls, shown as a badge on the Missed tab. The API has no unread state yet, so it stays 0. */
    val missedCount: Int = 0,
    /** Channel chips are multi-select; empty means no filter. Only used on the All tab. */
    val selectedChannels: Set<ActivityChannel> = emptySet(),
    val content: ActivityContent = ActivityContent.Loading,
    val isOffline: Boolean = false,
    /** Time of the cached data, shown in the offline banner. */
    val lastUpdatedLabel: String? = null,
    val isRefreshing: Boolean = false,
    /** The next page is being fetched. */
    val isLoadingMore: Boolean = false,
    /** The server has more pages for the current filter. */
    val canLoadMore: Boolean = false,
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
 * One row. The sub-line is "<kind label> · by [agentName] · [durationLabel]" (e.g. "Inbound · by Ana (Me) · 4:12");
 * missed, abandoned and not-answered calls show only the kind label, so both are null for them.
 */
data class ActivityItem(
    val id: String,
    val kind: ActivityKind,
    /** Contact name, number or address. Null shows "Unknown contact". */
    val title: String?,
    /** Agent who handled it. Null hides "by …". */
    val agentName: String?,
    /** [agentName] is the signed-in agent, shown as "(Me)". */
    val isMine: Boolean,
    /** Call length, `m:ss` or `h:mm:ss`. Null on messages and unanswered calls. */
    val durationLabel: String?,
    val timeLabel: String,
    val unreadCount: Int = 0,
)

sealed interface ActivityKind {
    data class Call(val direction: CallDirection) : ActivityKind
    data class Message(val channel: MessageChannel) : ActivityKind
}

enum class CallDirection { Inbound, Outbound, Missed, Abandoned, NotAnswered, Transfer }

enum class MessageChannel { Sms, WhatsApp, Email }

sealed interface ActivityIntent {
    data class SelectTab(val tab: ActivityTab) : ActivityIntent
    data class ToggleChannel(val channel: ActivityChannel) : ActivityIntent
    data object ClearFilters : ActivityIntent
    data object Refresh : ActivityIntent
    data object Retry : ActivityIntent
    data object LoadMore : ActivityIntent
    data class CallBack(val itemId: String) : ActivityIntent
    data class OpenItem(val itemId: String) : ActivityIntent
    data object NewCall : ActivityIntent
}
