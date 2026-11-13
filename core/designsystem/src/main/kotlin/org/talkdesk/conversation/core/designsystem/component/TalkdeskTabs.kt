package org.talkdesk.conversation.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

/**
 * Fixed tabs (`ta-tabs`): Material 3 [PrimaryTabRow], 48dp on `surface-1` with a 1dp
 * `border-divider` baseline and a 3dp `border-selected` indicator inset 12dp (it animates between
 * tabs). Put two to four [TalkdeskTab]s inside; [selectedTabIndex] is the active one.
 */
@Composable
fun TalkdeskTabs(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = TalkdeskTheme.colors
    PrimaryTabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier,
        containerColor = colors.surface1,
        contentColor = colors.textBrand,
        indicator = {
            Box(
                Modifier
                    .tabIndicatorOffset(selectedTabIndex, matchContentSize = false)
                    .padding(horizontal = 12.dp)
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(colors.borderSelected, TalkdeskTheme.shapes.xs),
            )
        },
        divider = { HorizontalDivider(thickness = 1.dp, color = colors.borderDivider) },
        tabs = content,
    )
}

/**
 * One tab (Material 3 [Tab]): `label` type, `text-brand` when [selected], `text-description`
 * otherwise. [badgeCount] shows an unread [TalkdeskBadge] after the label.
 */
@Composable
fun TalkdeskTab(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    badgeCount: Int? = null,
) {
    val colors = TalkdeskTheme.colors
    Tab(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        selectedContentColor = colors.textBrand,
        unselectedContentColor = colors.textDescription,
        text = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(label, style = TalkdeskTheme.typography.label, maxLines = 1)
                if (badgeCount != null && badgeCount > 0) TalkdeskBadge(TalkdeskBadgeValue.Count(badgeCount))
            }
        },
    )
}

@TalkdeskPreviews
@Composable
private fun TalkdeskTabsPreview() = PreviewSurface {
    TalkdeskTabs(selectedTabIndex = 0) {
        TalkdeskTab(selected = true, onClick = {}, label = "All")
        TalkdeskTab(selected = false, onClick = {}, label = "Missed", badgeCount = 3)
    }
}
