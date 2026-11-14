package org.talkdesk.conversation.calling.impl.ui.incoming

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.talkdesk.conversation.calling.impl.R
import org.talkdesk.conversation.calling.impl.presentation.incoming.IncomingCallIntent
import org.talkdesk.conversation.calling.impl.presentation.incoming.IncomingCallState
import org.talkdesk.conversation.calling.impl.ui.CallingPreviews
import org.talkdesk.conversation.calling.impl.ui.rememberCallingHaptics
import org.talkdesk.conversation.calling.impl.ui.PreviewContact
import org.talkdesk.conversation.core.designsystem.component.TalkdeskAvatar
import org.talkdesk.conversation.core.designsystem.component.TalkdeskAvatarSize
import org.talkdesk.conversation.core.designsystem.component.TalkdeskCallButtonKind
import org.talkdesk.conversation.core.designsystem.component.TalkdeskCallButtonWithCaption
import org.talkdesk.conversation.core.designsystem.component.TalkdeskCallRings
import org.talkdesk.conversation.core.designsystem.component.TalkdeskCallState
import org.talkdesk.conversation.core.designsystem.component.TalkdeskCallStateLabel
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

/**
 * Full-screen incoming call (shown over the lock screen): "Incoming call", the line that was called, the
 * caller inside the pulsing rings, then Decline on the left and Accept on the right, 96dp apart.
 * Stateless: renders [state] and reports taps as [IncomingCallIntent]s.
 */
@Composable
fun IncomingCallScreen(
    state: IncomingCallState,
    onIntent: (IncomingCallIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TalkdeskTheme.colors
    val haptics = rememberCallingHaptics()
    Column(modifier.fillMaxSize().background(colors.bgWindow).statusBarsPadding().navigationBarsPadding()) {
        Column(
            Modifier.fillMaxWidth().padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TalkdeskCallStateLabel(stringResource(R.string.calling_incoming_title), TalkdeskIcons.PhoneIn, colors.textBrand)
            Text(
                stringResource(R.string.calling_incoming_line, state.lineName),
                style = TalkdeskTheme.typography.labelRegular,
                color = colors.textDescription,
            )
            TalkdeskCallRings(TalkdeskCallState.Incoming, Modifier.padding(vertical = 24.dp), size = 192.dp) {
                TalkdeskAvatar(state.caller.initials, size = TalkdeskAvatarSize.Large)
            }
            Text(
                state.caller.name ?: stringResource(R.string.calling_unknown_caller),
                style = TalkdeskTheme.typography.h2.copy(fontWeight = FontWeight.Medium),
                color = colors.textDefault,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(state.caller.number, style = TalkdeskTheme.typography.body, color = colors.textDescription)
        }
        Spacer(Modifier.weight(1f))
        Row(
            Modifier.fillMaxWidth().height(200.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(96.dp, Alignment.CenterHorizontally),
        ) {
            TalkdeskCallButtonWithCaption(
                TalkdeskCallButtonKind.Reject,
                stringResource(R.string.calling_decline),
                onClick = {
                    haptics.reject()
                    onIntent(IncomingCallIntent.Decline)
                },
            )
            TalkdeskCallButtonWithCaption(
                TalkdeskCallButtonKind.Call,
                stringResource(R.string.calling_accept),
                onClick = {
                    haptics.confirm()
                    onIntent(IncomingCallIntent.Accept)
                },
            )
        }
    }
}

@CallingPreviews
@Composable
private fun IncomingCallPreview() = TalkdeskTheme {
    IncomingCallScreen(IncomingCallState(caller = PreviewContact.copy(phoneType = null), lineName = "Support line"), onIntent = {})
}
