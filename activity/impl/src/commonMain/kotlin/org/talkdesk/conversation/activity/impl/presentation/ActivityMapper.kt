package org.talkdesk.conversation.activity.impl.presentation

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.talkdesk.conversation.activity.impl.domain.model.Activity
import org.talkdesk.conversation.activity.impl.domain.model.ActivityType
import kotlin.time.Instant

/** Interaction types shown for a tab and channel selection. Channels only narrow the All tab. */
internal fun typesFor(tab: ActivityTab, channels: Set<ActivityChannel>): Set<ActivityType> = when {
    tab == ActivityTab.Missed -> setOf(ActivityType.MissedInbound)
    channels.isEmpty() -> ActivityChannel.entries.flatMapTo(mutableSetOf()) { it.types }
    else -> channels.flatMapTo(mutableSetOf()) { it.types }
}

private val ActivityChannel.types: Set<ActivityType>
    get() = when (this) {
        ActivityChannel.Calls -> setOf(
            ActivityType.Inbound, ActivityType.MissedInbound, ActivityType.Outbound, ActivityType.UnansweredOutbound,
            ActivityType.Abandoned, ActivityType.Transfer, ActivityType.OutboundAgent,
        )
        ActivityChannel.Sms -> setOf(ActivityType.InboundSms, ActivityType.OutboundSms)
        ActivityChannel.WhatsApp -> setOf(ActivityType.InboundWhatsApp, ActivityType.OutboundWhatsApp)
        ActivityChannel.Email -> setOf(ActivityType.InboundEmail, ActivityType.OutboundEmail)
    }

/** Groups [this] (newest first) into day sections in [timeZone]. */
internal fun List<Activity>.toSections(now: Instant, timeZone: TimeZone): List<ActivitySection> {
    val today = now.toLocalDateTime(timeZone).date
    val yesterday = today.minus(1, DateTimeUnit.DAY)
    return groupBy { it.startedAt.toLocalDateTime(timeZone).date }.map { (date, items) ->
        val day = when (date) {
            today -> ActivityDay.Today
            yesterday -> ActivityDay.Yesterday
            else -> ActivityDay.Date(date.toDisplay())
        }
        ActivitySection(day, items.map { it.toItem(timeZone) })
    }
}

private fun Activity.toItem(timeZone: TimeZone): ActivityItem {
    val kind = type.toKind()
    // Missed, abandoned and not-answered calls show only their label; answered calls show who and how long.
    val showDetails = kind !is ActivityKind.Call || kind.direction in ANSWERED
    return ActivityItem(
        id = id,
        kind = kind,
        title = contactName,
        agentName = agentName.takeIf { showDetails },
        isMine = isMine,
        durationLabel = if (kind is ActivityKind.Call && showDetails) durationSeconds.toDurationLabel() else null,
        timeLabel = startedAt.toLocalDateTime(timeZone).time.toDisplay(),
    )
}

private val ANSWERED = setOf(CallDirection.Inbound, CallDirection.Outbound, CallDirection.Transfer)

private fun ActivityType.toKind(): ActivityKind = when (this) {
    ActivityType.Inbound -> ActivityKind.Call(CallDirection.Inbound)
    ActivityType.MissedInbound -> ActivityKind.Call(CallDirection.Missed)
    ActivityType.Outbound, ActivityType.OutboundAgent -> ActivityKind.Call(CallDirection.Outbound)
    ActivityType.UnansweredOutbound -> ActivityKind.Call(CallDirection.NotAnswered)
    ActivityType.Abandoned -> ActivityKind.Call(CallDirection.Abandoned)
    ActivityType.Transfer -> ActivityKind.Call(CallDirection.Transfer)
    ActivityType.InboundSms, ActivityType.OutboundSms -> ActivityKind.Message(MessageChannel.Sms)
    ActivityType.InboundWhatsApp, ActivityType.OutboundWhatsApp -> ActivityKind.Message(MessageChannel.WhatsApp)
    ActivityType.InboundEmail, ActivityType.OutboundEmail -> ActivityKind.Message(MessageChannel.Email)
}

/** `m:ss`, or `h:mm:ss` from one hour. */
private fun Long.toDurationLabel(): String {
    val hours = this / 3600
    val minutes = this % 3600 / 60
    val seconds = this % 60
    return if (hours > 0) "$hours:${minutes.pad2()}:${seconds.pad2()}" else "$minutes:${seconds.pad2()}"
}

private fun Long.pad2() = toString().padStart(2, '0')

private fun Int.pad2() = toString().padStart(2, '0')

private fun LocalTime.toDisplay() = "${hour.pad2()}:${minute.pad2()}"

private fun LocalDate.toDisplay() = "${day.pad2()}/${(month.ordinal + 1).pad2()}/$year"

/** Label for the offline banner: the time when cached today, otherwise the date and time. */
internal fun Instant.toUpdatedLabel(now: Instant, timeZone: TimeZone): String {
    val local = toLocalDateTime(timeZone)
    val time = local.time.toDisplay()
    return if (local.date == now.toLocalDateTime(timeZone).date) time else "${local.date.toDisplay()} $time"
}
