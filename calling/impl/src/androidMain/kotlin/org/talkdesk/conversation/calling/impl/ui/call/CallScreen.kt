package org.talkdesk.conversation.calling.impl.ui.call

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.talkdesk.conversation.calling.impl.R
import org.talkdesk.conversation.calling.impl.presentation.call.CallIntent
import org.talkdesk.conversation.calling.impl.presentation.call.CallPhase
import org.talkdesk.conversation.calling.impl.domain.model.CallQuality
import org.talkdesk.conversation.calling.impl.presentation.call.CallState
import org.talkdesk.conversation.calling.impl.ui.CallingPreviews
import org.talkdesk.conversation.calling.impl.ui.rememberCallingHaptics
import org.talkdesk.conversation.calling.impl.ui.PreviewContact
import org.talkdesk.conversation.calling.impl.ui.call.components.CallControls
import org.talkdesk.conversation.calling.impl.ui.call.components.CallHeader
import org.talkdesk.conversation.calling.impl.ui.call.components.InCallKeypad
import org.talkdesk.conversation.core.designsystem.component.TalkdeskCallButtonKind
import org.talkdesk.conversation.core.designsystem.component.TalkdeskCallButtonWithCaption
import org.talkdesk.conversation.core.designsystem.component.TalkdeskCallQuality
import org.talkdesk.conversation.core.designsystem.component.TalkdeskCallQualityIndicator
import org.talkdesk.conversation.core.designsystem.component.TalkdeskIconButton
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

/**
 * The call screen for Calling, On call and On hold, with the call-quality indicator while connected and
 * the in-call keypad sliding up over the controls (300ms). Stateless: renders [state] and reports taps as
 * [CallIntent]s.
 */
@Composable
fun CallScreen(
    state: CallState,
    onIntent: (CallIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(enabled = state.isKeypadOpen) { onIntent(CallIntent.HideKeypad) }
    Box(modifier.fillMaxSize().background(TalkdeskTheme.colors.bgWindow)) {
        CallBody(state, onIntent)
        AnimatedVisibility(
            visible = state.isKeypadOpen,
            enter = slideInVertically(tween(300, easing = FastOutSlowInEasing)) { it },
            exit = slideOutVertically(tween(300, easing = FastOutSlowInEasing)) { it },
        ) {
            InCallKeypad(state, onIntent)
        }
    }
}

@Composable
private fun CallBody(state: CallState, onIntent: (CallIntent) -> Unit) {
    val haptics = rememberCallingHaptics()
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().height(56.dp).padding(start = 4.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TalkdeskIconButton(TalkdeskIcons.ChevDown, stringResource(R.string.calling_minimize), onClick = { onIntent(CallIntent.Minimize) })
            val quality = state.quality
            if (state.phase == CallPhase.OnCall && quality != null) {
                val label = stringResource(quality.labelRes())
                TalkdeskCallQualityIndicator(quality.toDesignSystem(), label)
            }
        }
        CallHeader(state, Modifier.padding(top = 8.dp))
        Spacer(Modifier.weight(1f))
        CallControls(state, onIntent, Modifier.padding(horizontal = 40.dp))
        Box(Modifier.fillMaxWidth().height(152.dp), contentAlignment = Alignment.Center) {
            TalkdeskCallButtonWithCaption(TalkdeskCallButtonKind.End, stringResource(R.string.calling_end), onClick = {
                haptics.reject()
                onIntent(CallIntent.EndCall)
            })
        }
    }
}

private fun CallQuality.labelRes() = when (this) {
    CallQuality.Excellent -> R.string.calling_quality_excellent
    CallQuality.Good -> R.string.calling_quality_good
    CallQuality.Poor -> R.string.calling_quality_poor
}

private fun CallQuality.toDesignSystem() = when (this) {
    CallQuality.Excellent -> TalkdeskCallQuality.Excellent
    CallQuality.Good -> TalkdeskCallQuality.Good
    CallQuality.Poor -> TalkdeskCallQuality.Poor
}

// Previews of boards 03 to 06 in "Dialer and calls · Android".

@CallingPreviews
@Composable
private fun CallCallingPreview() = TalkdeskTheme {
    CallScreen(CallState(phase = CallPhase.Calling, contact = PreviewContact), onIntent = {})
}

@CallingPreviews
@Composable
private fun CallActivePreview() = TalkdeskTheme {
    CallScreen(
        CallState(phase = CallPhase.OnCall, contact = PreviewContact, elapsedSeconds = 252, quality = CallQuality.Good),
        onIntent = {},
    )
}

@CallingPreviews
@Composable
private fun CallOnHoldMutedPreview() = TalkdeskTheme {
    CallScreen(
        CallState(phase = CallPhase.OnHold, contact = PreviewContact, elapsedSeconds = 337, isMuted = true),
        onIntent = {},
    )
}

@CallingPreviews
@Composable
private fun CallKeypadPreview() = TalkdeskTheme {
    CallScreen(
        CallState(phase = CallPhase.OnCall, contact = PreviewContact, elapsedSeconds = 362, isKeypadOpen = true, dtmfDigits = "12#"),
        onIntent = {},
    )
}
