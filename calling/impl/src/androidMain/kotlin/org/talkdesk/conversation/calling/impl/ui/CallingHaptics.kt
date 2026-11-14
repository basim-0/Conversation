package org.talkdesk.conversation.calling.impl.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * The haptic vocabulary of the calling screens, so every control feels the same everywhere. Only
 * deliberate taps vibrate: no passive events (ringing, timers, state changes the user did not cause).
 * The dial pad keys already vibrate themselves in the design system.
 */
@Immutable
internal class CallingHaptics(private val haptic: HapticFeedback) {
    /** Start or accept a call. */
    fun confirm() = haptic.performHapticFeedback(HapticFeedbackType.Confirm)

    /** Decline, end. */
    fun reject() = haptic.performHapticFeedback(HapticFeedbackType.Reject)

    /** A switch such as mute, hold or speaker; [isOn] is the state the tap is turning it into. */
    fun toggle(isOn: Boolean) =
        haptic.performHapticFeedback(if (isOn) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff)

    /** A light tick for small actions such as deleting a digit or opening the keypad. */
    fun tick() = haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)

    /** A long press that does something (clearing the number). */
    fun longPress() = haptic.performHapticFeedback(HapticFeedbackType.LongPress)
}

@Composable
internal fun rememberCallingHaptics(): CallingHaptics {
    val haptic = LocalHapticFeedback.current
    return remember(haptic) { CallingHaptics(haptic) }
}
