package org.talkdesk.conversation.calling.impl.ui.dialer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.talkdesk.conversation.calling.impl.R
import org.talkdesk.conversation.calling.impl.presentation.dialer.CallerId
import org.talkdesk.conversation.calling.impl.ui.CallingPreviews
import org.talkdesk.conversation.calling.impl.ui.PreviewSurface
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

/** "Calling from Default number · +1 555 0100": one quiet line, because the caller ID is not selectable. */
@Composable
internal fun CallerIdLine(callerId: CallerId, modifier: Modifier = Modifier) {
    val colors = TalkdeskTheme.colors
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(TalkdeskIcons.PhoneOut, contentDescription = null, tint = colors.textDescription, modifier = Modifier.size(TalkdeskTheme.sizes.iconSmall))
        Text(
            stringResource(R.string.calling_calling_from, callerId.label, callerId.number),
            style = TalkdeskTheme.typography.labelRegular,
            color = colors.textDescription,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@CallingPreviews
@Composable
private fun CallerIdLinePreview() = PreviewSurface { CallerIdLine(CallerId("Default number", "+1 555 0100")) }
