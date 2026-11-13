package org.talkdesk.conversation.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

/**
 * Filter chip (`ta-chip`): Material 3 [FilterChip] (32dp, 48dp touch target) with `radius-lg` and a
 * 1dp `border-control` outline on `surface-1`. Selected adds a check, a 2dp `border-selected`,
 * `surface-selected` fill and a `text-brand` 500 label. Chips are multi-select unless the screen
 * says otherwise.
 */
@Composable
fun TalkdeskFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = TalkdeskTheme.colors
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        label = {
            Text(
                text = label,
                style = if (selected) TalkdeskTheme.typography.label else TalkdeskTheme.typography.labelRegular,
                maxLines = 1,
            )
        },
        leadingIcon = if (selected) {
            { Icon(TalkdeskIcons.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
        } else {
            null
        },
        shape = TalkdeskTheme.shapes.lg,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = colors.surface1,
            labelColor = colors.textDefault,
            iconColor = colors.textBrand,
            disabledContainerColor = colors.surface1,
            disabledLabelColor = colors.textDisabled,
            disabledLeadingIconColor = colors.textDisabled,
            selectedContainerColor = colors.surfaceSelected,
            selectedLabelColor = colors.textBrand,
            selectedLeadingIconColor = colors.textBrand,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = enabled,
            selected = selected,
            borderColor = colors.borderControl,
            selectedBorderColor = colors.borderSelected,
            disabledBorderColor = colors.borderDivider,
            borderWidth = 1.dp,
            selectedBorderWidth = 2.dp,
        ),
    )
}

@TalkdeskPreviews
@Composable
private fun TalkdeskFilterChipPreview() = PreviewSurface {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TalkdeskFilterChip("Missed", selected = true, onClick = {})
        TalkdeskFilterChip("Inbound", selected = false, onClick = {})
        TalkdeskFilterChip("Abandoned", selected = false, onClick = {}, enabled = false)
    }
}
