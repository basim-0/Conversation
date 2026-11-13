package org.talkdesk.conversation.activity.impl.domain.model

import kotlin.time.Instant

/** One interaction in the agent's activity history: a call, SMS, WhatsApp or email. */
internal data class Activity(
    val id: String,
    val type: ActivityType,
    /** Contact name, or the number / address when the contact is unknown. Null when neither is known. */
    val contactName: String?,
    val contactNumber: String?,
    val agentName: String?,
    /** Handled by the signed-in agent. */
    val isMine: Boolean,
    val startedAt: Instant,
    val durationSeconds: Long,
)

/** Interaction types the history API returns, named as the API names them. */
internal enum class ActivityType(val apiName: String) {
    Inbound("INBOUND"),
    MissedInbound("MISSED_INBOUND"),
    Outbound("OUTBOUND"),
    UnansweredOutbound("UNANSWERED_OUTBOUND"),
    Abandoned("ABANDONED"),
    Transfer("TRANSFER"),
    OutboundAgent("OUTBOUND_AGENT"),
    InboundSms("INBOUND_SMS"),
    OutboundSms("OUTBOUND_SMS"),
    InboundEmail("INBOUND_EMAIL"),
    OutboundEmail("OUTBOUND_EMAIL"),
    InboundWhatsApp("INBOUND_WHATSAPP"),
    OutboundWhatsApp("OUTBOUND_WHATSAPP");

    companion object {
        /** Null for types this app does not show. */
        fun fromApi(name: String): ActivityType? = entries.firstOrNull { it.apiName == name }
    }
}
