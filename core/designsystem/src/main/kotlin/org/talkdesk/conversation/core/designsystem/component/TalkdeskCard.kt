package org.talkdesk.conversation.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskElevation
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme
import org.talkdesk.conversation.core.designsystem.theme.talkdeskShadow

/**
 * Grouped information (`ta-card`): `surface-1`, 1dp `border-divider`, `radius-md`, 16 padding,
 * 12 gap, `shadow-raised` in light theme (dark relies on the surface ladder).
 *
 * @param title optional header in `h4`; [headerIcon] precedes it and [headerTrailing] (e.g. an icon
 *   button) ends it.
 * @param emergency 2dp `callstate-emergency` outline and icon tint; only for emergency-calling info.
 */
@Composable
fun TalkdeskCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    headerIcon: (@Composable () -> Unit)? = null,
    headerTrailing: (@Composable () -> Unit)? = null,
    emergency: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = TalkdeskTheme.colors
    val shape = TalkdeskTheme.shapes.md
    Column(
        modifier = modifier
            .talkdeskShadow(TalkdeskElevation.Raised, shape)
            .background(colors.surface1, shape)
            .border(
                width = if (emergency) 2.dp else 1.dp,
                color = if (emergency) colors.callstateEmergency else colors.borderDivider,
                shape = shape,
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (title != null || headerTrailing != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                headerIcon?.invoke()
                if (title != null) {
                    Text(title, style = TalkdeskTheme.typography.h4, color = colors.textDefault, modifier = Modifier.weight(1f))
                }
                headerTrailing?.invoke()
            }
        }
        content()
    }
}

/**
 * Key/value row inside a [TalkdeskCard] (`ta-cell`): key in `caption`/`text-description`, value in
 * `body`. Every cell but the last ([showDivider] = false) is followed by a `border-divider` line.
 */
@Composable
fun TalkdeskCardCell(
    key: String,
    value: String,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
) {
    val colors = TalkdeskTheme.colors
    Column(modifier.fillMaxWidth()) {
        Text(key, style = TalkdeskTheme.typography.caption, color = colors.textDescription)
        Text(value, style = TalkdeskTheme.typography.body, color = colors.textDefault, modifier = Modifier.padding(top = 2.dp))
        if (showDivider) {
            HorizontalDivider(Modifier.padding(top = 12.dp), thickness = 1.dp, color = colors.borderDivider)
        }
    }
}

@TalkdeskPreviews
@Composable
private fun TalkdeskCardPreview() = PreviewSurface {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TalkdeskCard(title = "Contact details") {
            TalkdeskCardCell("Phone", "+1 555 0100")
            TalkdeskCardCell("Company", "Acme Inc.", showDivider = false)
            TalkdeskLink("Show more", {})
        }
        TalkdeskCard(
            title = "Emergency address",
            emergency = true,
            headerIcon = { Icon(TalkdeskIcons.Emergency, null, tint = TalkdeskTheme.colors.callstateEmergency, modifier = Modifier.size(24.dp)) },
        ) {
            TalkdeskCardCell("Address", "1 Example Street", showDivider = false)
        }
    }
}
