package org.talkdesk.conversation.calling.impl.ui.call.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.talkdesk.conversation.calling.impl.R
import org.talkdesk.conversation.calling.impl.presentation.call.CallIntent
import org.talkdesk.conversation.calling.impl.presentation.call.CallPhase
import org.talkdesk.conversation.calling.impl.presentation.call.CallState
import org.talkdesk.conversation.calling.impl.ui.rememberCallingHaptics
import org.talkdesk.conversation.core.designsystem.component.TalkdeskCallControl
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons

/**
 * The four agreed controls in a 2x2 grid: Mute, Hold, Speaker, Keypad. Active toggles flip their caption
 * to the action that undoes them ("Unmute", "Resume"). Hold and Keypad are disabled until the call
 * connects.
 */
@Composable
internal fun CallControls(state: CallState, onIntent: (CallIntent) -> Unit, modifier: Modifier = Modifier) {
    val onHold = state.phase == CallPhase.OnHold
    val haptics = rememberCallingHaptics()
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        ControlRow(
            first = {
            TalkdeskCallControl(
                icon = if (state.isMuted) TalkdeskIcons.MicOff else TalkdeskIcons.Mic,
                label = stringResource(if (state.isMuted) R.string.calling_control_unmute else R.string.calling_control_mute),
                active = state.isMuted,
                onClick = {
                    haptics.toggle(isOn = !state.isMuted)
                    onIntent(CallIntent.ToggleMute)
                },
            )
            },
            second = {
            TalkdeskCallControl(
                icon = if (onHold) TalkdeskIcons.Play else TalkdeskIcons.Pause,
                label = stringResource(if (onHold) R.string.calling_control_resume else R.string.calling_control_hold),
                active = onHold,
                onClick = {
                    haptics.toggle(isOn = !onHold)
                    onIntent(CallIntent.ToggleHold)
                },
                enabled = state.canUseConnectedControls,
            )
            },
        )
        ControlRow(
            first = {
            TalkdeskCallControl(
                icon = TalkdeskIcons.Speaker,
                label = stringResource(R.string.calling_control_speaker),
                active = state.isSpeakerOn,
                onClick = {
                    haptics.toggle(isOn = !state.isSpeakerOn)
                    onIntent(CallIntent.ToggleSpeaker)
                },
            )
            },
            second = {
            TalkdeskCallControl(
                icon = TalkdeskIcons.Dialpad,
                label = stringResource(R.string.calling_control_keypad),
                active = false,
                onClick = {
                    haptics.tick()
                    onIntent(CallIntent.ShowKeypad)
                },
                enabled = state.canUseConnectedControls,
            )
            },
        )
    }
}

/** Two equal cells with the control centred in each, 24dp apart. */
@Composable
private fun ControlRow(first: @Composable () -> Unit, second: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { first() }
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { second() }
    }
}
