package org.talkdesk.conversation.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

/** What a [TalkdeskBadge] shows: an unread count, or a bare "new" dot. */
sealed interface TalkdeskBadgeValue {
    data class Count(val count: Int) : TalkdeskBadgeValue
    data object Dot : TalkdeskBadgeValue
}

/**
 * Unread badge (`ta-badge`): `badge-unread` fill with `text-on-light-fill` count (20dp min, `caption`
 * 500). [TalkdeskBadgeValue.Dot] is the 8dp "new" marker. [compact] is the 16dp variant used on
 * navigation items.
 */
@Composable
fun TalkdeskBadge(
    value: TalkdeskBadgeValue,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val colors = TalkdeskTheme.colors
    when (value) {
        TalkdeskBadgeValue.Dot -> Box(modifier.size(8.dp).background(colors.badgeUnread, TalkdeskTheme.shapes.full))
        is TalkdeskBadgeValue.Count -> {
            val size = if (compact) 16.dp else 20.dp
            Box(
                modifier = modifier
                    .defaultMinSize(minWidth = size)
                    .height(size)
                    .background(colors.badgeUnread, TalkdeskTheme.shapes.full)
                    .padding(horizontal = if (compact) 4.dp else 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (value.count > 99) "99+" else value.count.toString(),
                    style = TalkdeskTheme.typography.overline.copy(
                        fontSize = if (compact) TalkdeskTheme.typography.navLabel.fontSize else TalkdeskTheme.typography.overline.fontSize,
                    ),
                    color = colors.textOnLightFill,
                    maxLines = 1,
                )
            }
        }
    }
}

@TalkdeskPreviews
@Composable
private fun TalkdeskBadgePreview() = PreviewSurface {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        TalkdeskBadge(TalkdeskBadgeValue.Count(3))
        TalkdeskBadge(TalkdeskBadgeValue.Count(12))
        TalkdeskBadge(TalkdeskBadgeValue.Count(120))
        TalkdeskBadge(TalkdeskBadgeValue.Count(3), compact = true)
        TalkdeskBadge(TalkdeskBadgeValue.Dot)
    }
}
