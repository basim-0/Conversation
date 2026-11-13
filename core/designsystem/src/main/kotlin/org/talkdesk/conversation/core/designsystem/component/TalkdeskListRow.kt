package org.talkdesk.conversation.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

/**
 * Two-line list row (`ta-row`), at least 64dp, used by contacts, activity, voicemail and agent
 * lists. [leading] is an avatar or [TalkdeskListRowIcon]; [trailing] an icon button or
 * [TalkdeskListRowMeta]. [showDivider] draws the inset divider (starts at 68dp) above the row.
 * [selected] (tablet master list) uses `surface-selected` and a 3dp edge marker. Pass [onClick] to
 * make the whole row tappable.
 */
@Composable
fun TalkdeskListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    selected: Boolean = false,
    showDivider: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val colors = TalkdeskTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val background = when {
        pressed -> colors.surfaceSunken
        selected -> colors.surfaceSelected
        else -> colors.surface1
    }
    Box(
        modifier
            .fillMaxWidth()
            .background(background)
            .let { if (onClick != null) it.clickable(interactionSource = interaction, indication = null, onClick = onClick) else it },
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = TalkdeskTheme.sizes.row)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            leading?.invoke()
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    title,
                    style = TalkdeskTheme.typography.bodyStrong,
                    color = colors.textDefault,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Text(
                        subtitle,
                        style = TalkdeskTheme.typography.labelRegular,
                        color = colors.textDescription,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            trailing?.invoke()
        }
        if (showDivider) {
            HorizontalDivider(Modifier.padding(start = 68.dp), thickness = 1.dp, color = colors.borderDivider)
        }
        if (selected) {
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight()
                    .padding(vertical = 8.dp)
                    .width(3.dp)
                    .background(colors.borderSelected, TalkdeskTheme.shapes.xs),
            )
        }
    }
}

/** Leading 40dp direction/channel icon slot (`ta-row__icon`). Tint it with a `call-*` token. */
@Composable
fun TalkdeskListRowIcon(
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Box(modifier.size(TalkdeskTheme.sizes.avatar), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(TalkdeskTheme.sizes.icon))
    }
}

/** Trailing time plus optional unread badge (`ta-row__meta`), end-aligned. */
@Composable
fun TalkdeskListRowMeta(
    time: String,
    modifier: Modifier = Modifier,
    badgeCount: Int? = null,
) {
    Column(modifier, horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(time, style = TalkdeskTheme.typography.caption, color = TalkdeskTheme.colors.textDescription, maxLines = 1)
        if (badgeCount != null && badgeCount > 0) TalkdeskBadge(TalkdeskBadgeValue.Count(badgeCount))
    }
}

/** Loading row with the real row geometry, so nothing jumps when data arrives. */
@Composable
fun TalkdeskListRowSkeleton(modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = TalkdeskTheme.sizes.row)
            .background(TalkdeskTheme.colors.surface1)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TalkdeskSkeleton(Modifier.size(TalkdeskTheme.sizes.avatar), TalkdeskTheme.shapes.full)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            TalkdeskSkeleton(Modifier.fillMaxWidth(0.6f).height(14.dp))
            TalkdeskSkeleton(Modifier.fillMaxWidth(0.4f).height(12.dp))
        }
    }
}

@TalkdeskPreviews
@Composable
private fun TalkdeskListRowPreview() = PreviewSurface {
    val colors = TalkdeskTheme.colors
    Column {
        TalkdeskListRow(
            title = "Maria Silva",
            subtitle = "Example Corp · Mobile",
            leading = { TalkdeskAvatar("MS") },
            trailing = { Icon(TalkdeskIcons.Phone, null, tint = colors.iconBrand, modifier = Modifier.size(24.dp)) },
        )
        TalkdeskListRow("James Taylor", subtitle = "Selected row", leading = { TalkdeskAvatar("JT") }, selected = true, showDivider = true)
        TalkdeskListRow(
            title = "+1 555 0187",
            subtitle = "Missed · Support line",
            leading = { TalkdeskListRowIcon(TalkdeskIcons.PhoneMissed, colors.callMissed) },
            trailing = { TalkdeskListRowMeta("10:42", badgeCount = 2) },
            showDivider = true,
        )
        TalkdeskListRow(
            title = "Maria Silva",
            subtitle = "Inbound · 4m 12s",
            leading = { TalkdeskListRowIcon(TalkdeskIcons.PhoneIn, colors.callInbound) },
            trailing = { TalkdeskListRowMeta("09:15") },
            showDivider = true,
        )
        TalkdeskListRowSkeleton()
    }
}
