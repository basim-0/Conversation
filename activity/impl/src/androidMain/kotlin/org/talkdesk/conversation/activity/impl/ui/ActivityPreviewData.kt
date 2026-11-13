package org.talkdesk.conversation.activity.impl.ui

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import org.talkdesk.conversation.activity.impl.presentation.ActivityChannel
import org.talkdesk.conversation.activity.impl.presentation.ActivityContent
import org.talkdesk.conversation.activity.impl.presentation.ActivityDay
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

/** Sample data for the Compose previews only. The running app gets its state from `ActivityViewModel`. */
internal object ActivityPreviewData {
    private fun call(
        id: String,
        direction: CallDirection,
        title: String?,
        time: String,
        agent: String? = null,
        isMine: Boolean = false,
        duration: String? = null,
    ) = ActivityItem(id, ActivityKind.Call(direction), title, agent, isMine, duration, time)

    private fun message(id: String, channel: MessageChannel, title: String, time: String, agent: String? = null, isMine: Boolean = false) =
        ActivityItem(id, ActivityKind.Message(channel), title, agent, isMine, durationLabel = null, timeLabel = time)

    private val sections = listOf(
        ActivitySection(
            ActivityDay.Today,
            listOf(
                call("t1", CallDirection.Missed, "+1 555 0187", "10:42"),
                call("t2", CallDirection.Inbound, "Maria Silva", "09:15", agent = "Ana Costa", isMine = true, duration = "4:12"),
                message("t3", MessageChannel.WhatsApp, "Jordan Lee", "08:50", agent = "Ana Costa", isMine = true),
                call("t4", CallDirection.Outbound, "Alex Chen", "08:12", agent = "Rui Santos", duration = "1:03"),
                call("t5", CallDirection.Missed, "Priya Nair", "09:58"),
                call("t6", CallDirection.Abandoned, "+1 555 0166", "09:31"),
                call("t7", CallDirection.NotAnswered, null, "08:04"),
            ),
        ),
        ActivitySection(
            ActivityDay.Yesterday,
            listOf(
                message("y1", MessageChannel.Email, "sam.rivera@example.com", "17:30", agent = "Rui Santos"),
                message("y2", MessageChannel.Sms, "+1 555 0123", "16:05", agent = "Ana Costa", isMine = true),
                call("y3", CallDirection.Transfer, "Priya Nair", "14:20", agent = "Rui Santos", duration = "12:40"),
            ),
        ),
    )

    private fun ActivityItem.matches(tab: ActivityTab, channels: Set<ActivityChannel>): Boolean {
        val k = kind
        if (tab == ActivityTab.Missed) {
            return k is ActivityKind.Call && k.direction == CallDirection.Missed
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
}
