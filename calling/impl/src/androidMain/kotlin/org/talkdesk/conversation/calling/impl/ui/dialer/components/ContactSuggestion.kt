package org.talkdesk.conversation.calling.impl.ui.dialer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.talkdesk.conversation.calling.impl.R
import org.talkdesk.conversation.calling.impl.presentation.CallContact
import org.talkdesk.conversation.calling.impl.ui.CallingPreviews
import org.talkdesk.conversation.calling.impl.ui.PreviewSurface
import org.talkdesk.conversation.calling.impl.ui.PreviewContact
import org.talkdesk.conversation.core.designsystem.component.TalkdeskAvatar
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

/**
 * Matching contact shown above the keypad while typing: 64dp, `surface-1`, `border-divider`, `radius-md`
 * with the avatar, name, "Mobile · number" and a "Use" action. Private to the calling feature.
 */
@Composable
internal fun ContactSuggestion(contact: CallContact, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = TalkdeskTheme.colors
    val shape = TalkdeskTheme.shapes.md
    Row(
        modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(shape)
            .background(colors.surface1, shape)
            .border(1.dp, colors.borderDivider, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TalkdeskAvatar(initials = contact.initials)
        Column(Modifier.weight(1f)) {
            Text(
                contact.name ?: contact.number,
                style = TalkdeskTheme.typography.bodyStrong,
                color = colors.textDefault,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                phoneLine(contact),
                style = TalkdeskTheme.typography.labelRegular,
                color = colors.textDescription,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(stringResource(R.string.calling_use_contact), style = TalkdeskTheme.typography.label, color = colors.textBrand)
    }
}

/** "Mobile · +1 555 0142", or just the number when the contact has no phone type. */
@Composable
internal fun phoneLine(contact: CallContact): String =
    contact.phoneType?.let { stringResource(R.string.calling_contact_line, it, contact.number) } ?: contact.number

@CallingPreviews
@Composable
private fun ContactSuggestionPreview() = PreviewSurface { ContactSuggestion(PreviewContact, onClick = {}) }
