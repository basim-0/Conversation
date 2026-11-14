package org.talkdesk.conversation.calling.impl.presentation.incoming

import org.talkdesk.conversation.calling.impl.presentation.CallContact

/**
 * Data contract of the full-screen incoming call. [IncomingCallViewModel] owns an [IncomingCallState]
 * in one `StateFlow`, receives [IncomingCallIntent]s and sends one-shot [IncomingCallEffect]s.
 */
data class IncomingCallState(
    val caller: CallContact = CallContact(name = null, initials = null, phoneType = null, number = ""),
    /** The line that was called (queue), so agents know which one is ringing. */
    val lineName: String = "",
)

sealed interface IncomingCallIntent {
    data object Accept : IncomingCallIntent
    data object Decline : IncomingCallIntent
}

sealed interface IncomingCallEffect {
    /** Open the call screen. */
    data object Accepted : IncomingCallEffect

    /** Close the incoming screen. */
    data object Declined : IncomingCallEffect
}
