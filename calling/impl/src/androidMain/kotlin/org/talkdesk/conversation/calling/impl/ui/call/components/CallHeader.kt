package org.talkdesk.conversation.calling.impl.ui.call.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.talkdesk.conversation.calling.impl.R
import org.talkdesk.conversation.calling.impl.presentation.CallContact
import org.talkdesk.conversation.calling.impl.presentation.call.CallPhase
import org.talkdesk.conversation.calling.impl.presentation.call.CallState
import org.talkdesk.conversation.calling.impl.ui.CallingPreviews
import org.talkdesk.conversation.calling.impl.ui.PreviewSurface
import org.talkdesk.conversation.calling.impl.ui.PreviewContact
import org.talkdesk.conversation.calling.impl.ui.dialer.components.phoneLine
import org.talkdesk.conversation.core.designsystem.component.TalkdeskAvatar
import org.talkdesk.conversation.core.designsystem.component.TalkdeskAvatarSize
import org.talkdesk.conversation.core.designsystem.component.TalkdeskCallRings
import org.talkdesk.conversation.core.designsystem.component.TalkdeskCallState
import org.talkdesk.conversation.core.designsystem.component.TalkdeskCallStateLabel
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

/**
 * Top of the call screen: state label (icon and word, never colour alone), the 96dp avatar inside the
 * call-state rings, the contact name and either their number (calling) or the call timer. On hold the
 * label becomes a warning chip, the rings stop and the avatar dims.
 */
@Composable
internal fun CallHeader(state: CallState, modifier: Modifier = Modifier) {
    val colors = TalkdeskTheme.colors
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        when (state.phase) {
            CallPhase.Calling -> TalkdeskCallStateLabel(stringResource(R.string.calling_state_calling), TalkdeskIcons.PhoneOut, colors.textInfo)
            CallPhase.OnCall -> TalkdeskCallStateLabel(stringResource(R.string.calling_state_on_call), TalkdeskIcons.Phone, colors.textSuccess)
            CallPhase.OnHold -> OnHoldChip()
        }
        when (state.phase) {
            CallPhase.OnHold -> Box(Modifier.padding(vertical = 12.dp).size(168.dp), contentAlignment = Alignment.Center) {
                TalkdeskAvatar(state.contact.initials, Modifier.alpha(0.6f), size = TalkdeskAvatarSize.Large)
            }
            else -> TalkdeskCallRings(
                state = if (state.phase == CallPhase.Calling) TalkdeskCallState.Dialing else TalkdeskCallState.OnCall,
                modifier = Modifier.padding(vertical = 16.dp),
            ) {
                TalkdeskAvatar(state.contact.initials, size = TalkdeskAvatarSize.Large)
            }
        }
        Text(
            state.contact.name ?: state.contact.number,
            style = TalkdeskTheme.typography.h2.copy(fontWeight = FontWeight.Medium),
            color = colors.textDefault,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (state.phase == CallPhase.Calling) {
            Text(phoneLine(state.contact), style = TalkdeskTheme.typography.body, color = colors.textDescription)
        } else {
            Text(formatElapsed(state.elapsedSeconds), style = TalkdeskTheme.typography.timer, color = colors.textDescription)
        }
    }
}

@Composable
private fun OnHoldChip() {
    val colors = TalkdeskTheme.colors
    Row(
        Modifier
            .height(32.dp)
            .background(colors.fillWarningSubtle, TalkdeskTheme.shapes.lg)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(TalkdeskIcons.Pause, contentDescription = null, tint = colors.textWarning, modifier = Modifier.size(TalkdeskTheme.sizes.iconSmall))
        Text(stringResource(R.string.calling_state_on_hold), style = TalkdeskTheme.typography.label, color = colors.textWarning)
    }
}

@CallingPreviews
@Composable
private fun CallHeaderPreview() = PreviewSurface {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        CallHeader(CallState(phase = CallPhase.OnHold, contact = PreviewContact, elapsedSeconds = 337))
    }
}
