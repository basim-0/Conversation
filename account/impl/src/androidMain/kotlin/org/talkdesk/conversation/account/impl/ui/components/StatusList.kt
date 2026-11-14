package org.talkdesk.conversation.account.impl.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.talkdesk.conversation.account.impl.R
import org.talkdesk.conversation.account.impl.presentation.AccountMock
import org.talkdesk.conversation.account.impl.presentation.model.AgentStatus
import org.talkdesk.conversation.account.impl.presentation.model.StatusGroup
import org.talkdesk.conversation.account.impl.presentation.model.StatusGroupKind
import org.talkdesk.conversation.account.impl.ui.AccountPreviews
import org.talkdesk.conversation.account.impl.ui.PreviewSurface
import org.talkdesk.conversation.core.designsystem.component.TalkdeskSkeleton
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme
import androidx.compose.ui.semantics.Role

/**
 * Statuses grouped by what they mean (Available, Away, Offline, Set automatically). Each status is a
 * 48dp radio row; the system-set group is disabled and explained, not just greyed out.
 */
@Composable
internal fun StatusList(
    groups: List<StatusGroup>,
    selectedStatusId: String?,
    enabled: Boolean,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth().selectableGroup()) {
        groups.forEach { group ->
            GroupHeader(stringResource(group.kind.titleRes()))
            group.statuses.forEach { status ->
                StatusRow(
                    status = status,
                    selected = status.id == selectedStatusId,
                    enabled = enabled && !status.isSystemSet,
                    onClick = { onSelect(status.id) },
                )
            }
        }
    }
}

@Composable
private fun GroupHeader(title: String) {
    Text(
        text = title,
        style = TalkdeskTheme.typography.label.copy(fontSize = 13.sp, lineHeight = 18.sp),
        color = TalkdeskTheme.colors.textDescription,
        modifier = Modifier.fillMaxWidth().padding(start = 52.dp, top = 12.dp, end = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun StatusRow(status: AgentStatus, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val colors = TalkdeskTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .background(if (selected) colors.surfaceSelected else Color.Transparent)
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .padding(start = 20.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        StatusIndicator(status.presence)
        Column(Modifier.weight(1f)) {
            Text(
                text = status.label,
                style = if (selected) TalkdeskTheme.typography.bodyStrong else TalkdeskTheme.typography.body,
                color = if (status.isSystemSet) colors.textDescription else colors.textDefault,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (status.isSystemSet) {
                Text(
                    stringResource(R.string.account_status_automatic_note),
                    style = TalkdeskTheme.typography.caption,
                    color = colors.textDescription,
                )
            }
        }
        if (selected) {
            Icon(TalkdeskIcons.Check, contentDescription = null, tint = colors.iconBrand, modifier = Modifier.size(24.dp))
        }
    }
}

/** Skeleton rows that keep the layout while the statuses load. */
@Composable
internal fun StatusListSkeleton(modifier: Modifier = Modifier, rows: Int = 6) {
    Column(modifier.fillMaxWidth()) {
        repeat(rows) {
            Row(
                Modifier.fillMaxWidth().height(48.dp).padding(start = 20.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                TalkdeskSkeleton(Modifier.size(12.dp), TalkdeskTheme.shapes.full)
                TalkdeskSkeleton(Modifier.width(140.dp).height(12.dp))
            }
        }
    }
}

private fun StatusGroupKind.titleRes() = when (this) {
    StatusGroupKind.Available -> R.string.account_group_available
    StatusGroupKind.Away -> R.string.account_group_away
    StatusGroupKind.Offline -> R.string.account_group_offline
    StatusGroupKind.Automatic -> R.string.account_group_automatic
}

@AccountPreviews
@Composable
private fun StatusListPreview() = PreviewSurface {
    Column {
        StatusList(AccountMock.statusGroups, selectedStatusId = "away-lunch", enabled = true, onSelect = {})
    }
}
