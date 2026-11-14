package org.talkdesk.conversation.calling.impl.presentation.call

import org.talkdesk.conversation.calling.impl.presentation.CallContact

/**
 * Data contract of the call screen, which moves through [CallPhase.Calling], [CallPhase.OnCall] and
 * [CallPhase.OnHold]. [CallViewModel] owns a [CallState] in one `StateFlow`, receives [CallIntent]s and
 * sends one-shot [CallEffect]s.
 */
data class CallState(
    val phase: CallPhase = CallPhase.Calling,
    val contact: CallContact = CallContact(name = null, initials = null, phoneType = null, number = ""),
    /** Call length; only counts once connected. */
    val elapsedSeconds: Int = 0,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    /** The in-call (DTMF) keypad covers the controls. */
    val isKeypadOpen: Boolean = false,
    /** Tones sent so far in this call, shown in the keypad readout. */
    val dtmfDigits: String = "",
    /** Null until the network reports it (and while calling). */
    val quality: CallQuality? = null,
) {
    /** Hold and keypad need a connected call. */
    val canUseConnectedControls: Boolean get() = phase != CallPhase.Calling
}

enum class CallPhase { Calling, OnCall, OnHold }

enum class CallQuality { Excellent, Good, Poor }

sealed interface CallIntent {
    data object ToggleMute : CallIntent
    data object ToggleHold : CallIntent
    data object ToggleSpeaker : CallIntent
    data object ShowKeypad : CallIntent
    data object HideKeypad : CallIntent
    data class SendDigit(val digit: Char) : CallIntent
    data object EndCall : CallIntent
    data object Minimize : CallIntent
}

sealed interface CallEffect {
    /** The call ended after [durationSeconds]; leave the call screen. */
    data class CallEnded(val durationSeconds: Int) : CallEffect

    /** Back to the app with the ongoing-call chip. */
    data object Minimized : CallEffect
}
