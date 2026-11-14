package org.talkdesk.conversation.calling.impl.ui.call.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.talkdesk.conversation.calling.impl.R
import org.talkdesk.conversation.calling.impl.presentation.call.CallIntent
import org.talkdesk.conversation.calling.impl.presentation.call.CallPhase
import org.talkdesk.conversation.calling.impl.presentation.call.CallState
import org.talkdesk.conversation.core.designsystem.component.TalkdeskAvatar
import org.talkdesk.conversation.core.designsystem.component.TalkdeskButton
import org.talkdesk.conversation.core.designsystem.component.TalkdeskButtonVariant
import org.talkdesk.conversation.core.designsystem.component.TalkdeskCallButtonKind
import org.talkdesk.conversation.core.designsystem.component.TalkdeskCallButtonWithCaption
import org.talkdesk.conversation.core.designsystem.component.TalkdeskDialpad
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

/**
 * The in-call (DTMF) keypad: the design-system dial pad over the controls, with the caller in a compact
 * header, the sent tones in a live readout, End in the middle and Hide at the end. System back also hides it.
 */
@Composable
internal fun InCallKeypad(state: CallState, onIntent: (CallIntent) -> Unit, modifier: Modifier = Modifier) {
    val colors = TalkdeskTheme.colors
    Column(modifier.fillMaxSize().background(colors.bgWindow).statusBarsPadding().navigationBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TalkdeskAvatar(state.contact.initials)
            Column(Modifier.weight(1f)) {
                Text(state.contact.name ?: state.contact.number, style = TalkdeskTheme.typography.bodyStrong, color = colors.textDefault, maxLines = 1)
                Text(
                    text = stringResource(
                        R.string.calling_state_time,
                        stringResource(if (state.phase == CallPhase.OnHold) R.string.calling_state_on_hold else R.string.calling_state_on_call),
                        formatElapsed(state.elapsedSeconds),
                    ),
                    style = TalkdeskTheme.typography.labelRegular,
                    color = colors.textSuccess,
                )
            }
        }
        Text(
            text = state.dtmfDigits.toList().joinToString(" "),
            style = TalkdeskTheme.typography.h1.copy(fontWeight = FontWeight.Normal, letterSpacing = 4.sp),
            color = colors.textDefault,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 34.dp)
                .padding(start = 16.dp, top = 32.dp, end = 16.dp)
                .semantics { liveRegion = LiveRegionMode.Polite },
        )
        Spacer(Modifier.weight(1f))
        TalkdeskDialpad(onKey = { onIntent(CallIntent.SendDigit(it)) }, modifier = Modifier.padding(horizontal = 24.dp))
        Row(Modifier.fillMaxWidth().height(152.dp).padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.weight(1f))
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                TalkdeskCallButtonWithCaption(TalkdeskCallButtonKind.End, stringResource(R.string.calling_end), onClick = { onIntent(CallIntent.EndCall) })
            }
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                TalkdeskButton(
                    stringResource(R.string.calling_keypad_hide),
                    onClick = { onIntent(CallIntent.HideKeypad) },
                    variant = TalkdeskButtonVariant.Ghost,
                )
            }
        }
    }
}
