package org.talkdesk.conversation.activity.impl.ui

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import org.talkdesk.conversation.activity.impl.presentation.ActivityChannel
import org.talkdesk.conversation.activity.impl.presentation.ActivityContent
import org.talkdesk.conversation.activity.impl.presentation.ActivityDay
import org.talkdesk.conversation.activity.impl.presentation.ActivityIntent
import org.talkdesk.conversation.activity.impl.presentation.ActivityItem
import org.talkdesk.conversation.activity.impl.presentation.ActivityKind
import org.talkdesk.conversation.activity.impl.presentation.ActivitySection
import org.talkdesk.conversation.activity.impl.presentation.ActivityState
import org.talkdesk.conversation.activity.impl.presentation.ActivityTab
import org.talkdesk.conversation.activity.impl.presentation.CallDirection
import org.talkdesk.conversation.activity.impl.presentation.MessageChannel

/** Phone-sized preview in both themes, matching the 360×800 Android board frames. */
@Preview(name = "Light", widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES)
internal annotation class ActivityPreviews

/**
 * Static data and a tiny reducer so previews and the running app can show every Activity state.
 * TODO: delete when the ViewModel and repository exist; the ViewModel then produces [ActivityState].
 */
internal object ActivityMock {
    private fun call(id: String, direction: CallDirection, title: String, detail: String, time: String) =
        ActivityItem(id, ActivityKind.Call(direction), title, detail, time)

    private fun message(id: String, channel: MessageChannel, title: String, detail: String, time: String, unread: Int = 0) =
        ActivityItem(id, ActivityKind.Message(channel), title, detail, time, unread)

    private val sections = listOf(
        ActivitySection(
            ActivityDay.Today,
            listOf(
                call("t1", CallDirection.Missed, "+1 555 0187", "Support line", "10:42"),
                call("t2", CallDirection.Inbound, "Maria Silva", "4m 12s", "09:15"),
                message("t3", MessageChannel.WhatsApp, "Jordan Lee", "Sure, I can check the order", "08:50", unread = 2),
                call("t4", CallDirection.Outbound, "Alex Chen", "1m 03s", "08:12"),
                call("t5", CallDirection.Missed, "Priya Nair", "Billing line", "09:58"),
                call("t6", CallDirection.Abandoned, "+1 555 0166", "Support line", "09:31"),
                call("t7", CallDirection.Missed, "Tom Becker", "Sales line", "08:04"),
            ),
        ),
        ActivitySection(
            ActivityDay.Yesterday,
            listOf(
                message("y1", MessageChannel.Email, "sam.rivera@example.com", "Re: Invoice 1042", "17:30"),
                message("y2", MessageChannel.Sms, "+1 555 0123", "Thanks for the update", "16:05"),
                call("y3", CallDirection.Abandoned, "Priya Nair", "Billing line", "14:20"),
            ),
        ),
    )

    private fun ActivityItem.matches(tab: ActivityTab, channels: Set<ActivityChannel>): Boolean {
        val k = kind
        if (tab == ActivityTab.Missed) {
            return k is ActivityKind.Call && (k.direction == CallDirection.Missed || k.direction == CallDirection.Abandoned)
        }
        if (channels.isEmpty()) return true
        val channel = when (k) {
            is ActivityKind.Call -> ActivityChannel.Calls
            is ActivityKind.Message -> when (k.channel) {
                MessageChannel.Sms -> ActivityChannel.Sms
                MessageChannel.WhatsApp -> ActivityChannel.WhatsApp
                MessageChannel.Email -> ActivityChannel.Email
            }
        }
        return channel in channels
    }

    fun loaded(
        tab: ActivityTab = ActivityTab.All,
        channels: Set<ActivityChannel> = emptySet(),
    ): ActivityContent.Loaded = ActivityContent.Loaded(
        sections.map { it.copy(items = it.items.filter { item -> item.matches(tab, channels) }) }
            .filter { it.items.isNotEmpty() },
    )

    fun state(
        tab: ActivityTab = ActivityTab.All,
        channels: Set<ActivityChannel> = emptySet(),
        content: ActivityContent = loaded(tab, channels),
        isOffline: Boolean = false,
        isRefreshing: Boolean = false,
    ) = ActivityState(
        selectedTab = tab,
        missedCount = 3,
        selectedChannels = channels,
        content = content,
        isOffline = isOffline,
        lastUpdatedLabel = if (isOffline) "10:45" else null,
        isRefreshing = isRefreshing,
    )

    fun reduce(current: ActivityState, intent: ActivityIntent): ActivityState = when (intent) {
        is ActivityIntent.SelectTab -> state(intent.tab)
        is ActivityIntent.ToggleChannel -> {
            val channels = current.selectedChannels.let { if (intent.channel in it) it - intent.channel else it + intent.channel }
            state(current.selectedTab, channels)
        }
        ActivityIntent.ClearFilters -> state(current.selectedTab)
        ActivityIntent.Retry -> state(current.selectedTab, current.selectedChannels)
        else -> current
    }
}
