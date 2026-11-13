package org.talkdesk.conversation.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.ShortNavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

/**
 * Phone navigation bar (`ta-bottomnav`): Material 3 [ShortNavigationBar] (64dp, system
 * navigation-bar inset handled) on `surface-nav` with a 1dp `border-divider` top edge. Fill it with
 * [TalkdeskBottomNavItem]s.
 */
@Composable
fun TalkdeskBottomNav(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = TalkdeskTheme.colors
    Column(modifier) {
        HorizontalDivider(thickness = 1.dp, color = colors.borderDivider)
        ShortNavigationBar(
            containerColor = colors.surfaceNav,
            contentColor = colors.iconMuted,
            content = content,
        )
    }
}

/**
 * One destination: Material 3 [ShortNavigationBarItem] with a 24dp icon, a `nav-label` and a
 * `surface-selected` indicator. Active uses `icon-brand`/`text-brand`, inactive `icon-muted`,
 * disabled `text-disabled`. [badge] sits on the icon.
 *
 * Differs from the design-system card only in the indicator height (M3: 32dp, card: 28dp).
 */
@Composable
fun TalkdeskBottomNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    badge: TalkdeskBadgeValue? = null,
) {
    val colors = TalkdeskTheme.colors
    ShortNavigationBarItem(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        icon = {
            if (badge != null) {
                BadgedBox(
                    badge = {
                        // M3 centres the badge on the icon's corner, so it covers the icon. Push it outward.
                        val outward = if (badge is TalkdeskBadgeValue.Dot) 6.dp else 8.dp
                        TalkdeskBadge(badge, compact = true, modifier = Modifier.offset(x = outward, y = (-2).dp))
                    },
                ) { Icon(icon, contentDescription = null) }
            } else {
                Icon(icon, contentDescription = null)
            }
        },
        label = { Text(label, style = TalkdeskTheme.typography.navLabel, maxLines = 1) },
        colors = ShortNavigationBarItemDefaults.colors(
            selectedIconColor = colors.iconBrand,
            selectedTextColor = colors.textBrand,
            selectedIndicatorColor = colors.surfaceSelected,
            unselectedIconColor = colors.iconMuted,
            unselectedTextColor = colors.iconMuted,
            disabledIconColor = colors.textDisabled,
            disabledTextColor = colors.textDisabled,
        ),
    )
}

@TalkdeskPreviews
@Composable
private fun TalkdeskBottomNavPreview() = PreviewSurface {
    TalkdeskBottomNav {
        TalkdeskBottomNavItem(selected = true, onClick = {}, icon = TalkdeskIcons.Clock, label = "Activity")
        TalkdeskBottomNavItem(selected = false, onClick = {}, icon = TalkdeskIcons.Contacts, label = "Contacts")
        TalkdeskBottomNavItem(selected = false, onClick = {}, icon = TalkdeskIcons.Message, label = "Messages", badge = TalkdeskBadgeValue.Count(3))
        TalkdeskBottomNavItem(selected = false, onClick = {}, icon = TalkdeskIcons.Voicemail, label = "Voicemail", badge = TalkdeskBadgeValue.Dot)
        TalkdeskBottomNavItem(selected = false, onClick = {}, icon = TalkdeskIcons.User, label = "Account", enabled = false)
    }
}
