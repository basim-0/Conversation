package org.talkdesk.conversation.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

/**
 * Icon-only button (`ta-iconbtn`): 48dp touch target around a 24dp icon, round ripple area.
 * [contentDescription] is required because there is no visible label. [tint] defaults to `icon-default`;
 * pass `TalkdeskTheme.colors.iconBrand` for the brand variant.
 */
@Composable
fun TalkdeskIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = TalkdeskTheme.colors.iconDefault,
) {
    Box(
        modifier
            .size(TalkdeskTheme.sizes.touch)
            .clip(TalkdeskTheme.shapes.full)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (enabled) tint else TalkdeskTheme.colors.textDisabled,
            modifier = Modifier.size(TalkdeskTheme.sizes.icon),
        )
    }
}

@TalkdeskPreviews
@Composable
private fun TalkdeskIconButtonPreview() = PreviewSurface {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TalkdeskIconButton(TalkdeskIcons.Back, "Back", {})
        TalkdeskIconButton(TalkdeskIcons.Contacts, "Contacts", {}, tint = TalkdeskTheme.colors.iconBrand)
        TalkdeskIconButton(TalkdeskIcons.More, "More", {}, enabled = false)
    }
}
