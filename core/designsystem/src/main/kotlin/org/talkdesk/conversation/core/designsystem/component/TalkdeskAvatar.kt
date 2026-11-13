package org.talkdesk.conversation.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

/** Agent presence. Each state has its own shape as well as colour, so it never relies on hue alone. */
enum class TalkdeskPresence { Available, Busy, Away, Offline }

enum class TalkdeskAvatarSize { Default, Large }

/**
 * Initials disc (`ta-avatar`): `surface-brand-subtle` with `text-brand` initials, 40dp (96dp
 * [TalkdeskAvatarSize.Large] for call and profile screens). Without [initials] it shows [icon].
 * [presence] adds a dot holder with a 2dp (3dp large) `surface-1` ring.
 */
@Composable
fun TalkdeskAvatar(
    initials: String?,
    modifier: Modifier = Modifier,
    icon: ImageVector = TalkdeskIcons.User,
    size: TalkdeskAvatarSize = TalkdeskAvatarSize.Default,
    presence: TalkdeskPresence? = null,
) {
    val colors = TalkdeskTheme.colors
    val large = size == TalkdeskAvatarSize.Large
    val diameter = if (large) TalkdeskTheme.sizes.avatarLarge else TalkdeskTheme.sizes.avatar
    Box(modifier.size(diameter)) {
        Box(
            Modifier.size(diameter).background(colors.surfaceBrandSubtle, TalkdeskTheme.shapes.full),
            contentAlignment = Alignment.Center,
        ) {
            if (initials.isNullOrBlank()) {
                Icon(icon, null, tint = colors.textBrand, modifier = Modifier.size(if (large) 40.dp else 20.dp))
            } else {
                Text(
                    text = initials,
                    color = colors.textBrand,
                    style = if (large) TalkdeskTheme.typography.h2 else TalkdeskTheme.typography.label,
                )
            }
        }
        if (presence != null) {
            val holder = if (large) 24.dp else 14.dp
            val ring = if (large) 3.dp else 2.dp
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 1.dp, y = 1.dp)
                    .size(holder)
                    .background(colors.surface1, TalkdeskTheme.shapes.full)
                    .border(ring, colors.surface1, TalkdeskTheme.shapes.full),
                contentAlignment = Alignment.Center,
            ) {
                PresenceDot(presence, if (large) 18.dp else 10.dp)
            }
        }
    }
}

@Composable
internal fun PresenceDot(presence: TalkdeskPresence, size: Dp, modifier: Modifier = Modifier) {
    val colors = TalkdeskTheme.colors
    val color: Color = when (presence) {
        TalkdeskPresence.Available -> colors.presenceAvailable
        TalkdeskPresence.Busy -> colors.presenceBusy
        TalkdeskPresence.Away -> colors.presenceAway
        TalkdeskPresence.Offline -> colors.presenceOffline
    }
    val shape = if (presence == TalkdeskPresence.Busy) TalkdeskTheme.shapes.xs else TalkdeskTheme.shapes.full
    val base = modifier.size(size)
    when (presence) {
        TalkdeskPresence.Away -> Box(base.border(3.dp, color, shape))
        TalkdeskPresence.Offline -> Box(base.border(2.dp, color, shape))
        else -> Box(base.background(color, shape))
    }
}

@TalkdeskPreviews
@Composable
private fun TalkdeskAvatarPreview() = PreviewSurface {
    Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
        TalkdeskAvatar("MS")
        TalkdeskAvatar("MS", presence = TalkdeskPresence.Available)
        TalkdeskAvatar("JT", presence = TalkdeskPresence.Busy)
        TalkdeskAvatar(null, presence = TalkdeskPresence.Away)
        TalkdeskAvatar("RK", presence = TalkdeskPresence.Offline)
        TalkdeskAvatar("MS", size = TalkdeskAvatarSize.Large, presence = TalkdeskPresence.Away)
    }
}
