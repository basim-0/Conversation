package org.talkdesk.conversation.calling.impl.domain

import org.talkdesk.conversation.calling.impl.domain.model.CallDirection
import org.talkdesk.conversation.calling.impl.domain.model.CallQuality
import org.talkdesk.conversation.calling.impl.domain.model.EndReason

internal enum class CallStatus { Ringing, Dialing, Connecting, Active, OnHold, Reconnecting }

/** One call the agent has or is being offered. Lives in memory only: it must not survive the process. */
internal data class Call(
    val id: String,
    val direction: CallDirection,
    val status: CallStatus,
    /** The other party's number, as dialled or as announced by the network. */
    val number: String,
    val contactName: String? = null,
    val phoneType: String? = null,
    /** The line (ring group) that was called. */
    val lineName: String? = null,
    /** Handle of the call inside the voice network adapter. Null until it is created. */
    val voiceHandle: String? = null,
    /** Provider (Twilio) call sid, the key of `calls-info` and of server-side hang-up. */
    val providerCallId: String? = null,
    /** Talkdesk interaction id, the key of hold/unhold. Known once `calls-info` answers. */
    val interactionId: String? = null,
    val connectedAtMillis: Long? = null,
    val isMuted: Boolean = false,
    val quality: CallQuality? = null,
    /** The agent asked to end it: the next disconnect is local. */
    val endRequested: Boolean = false,
) {
    val isRinging: Boolean get() = status == CallStatus.Ringing
}

internal sealed interface CallEvent {
    /** [call] is gone from the list. */
    data class Ended(val call: Call, val reason: EndReason, val failure: String? = null) : CallEvent
}

/** The agent cannot place a call because one is already in progress. */
internal class CallBusyException : Exception("A call is already in progress")
