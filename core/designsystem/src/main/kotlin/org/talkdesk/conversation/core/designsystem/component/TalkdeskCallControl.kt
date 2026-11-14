package org.talkdesk.conversation.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

/** 13/18 Medium caption under call buttons and controls (`ta-ctrl`, `ta-callbtn-wrap`). */
@Composable
internal fun callCaptionStyle(): TextStyle = TalkdeskTheme.typography.label.copy(fontSize = 13.sp, lineHeight = 18.sp)

/**
 * On-call utility control (`ta-ctrl`): a 56dp toggle with a caption underneath.
 *
 * Inactive: `surface-3` disc, `icon-default` glyph, `border-divider` outline. [active] (muted, on hold,
 * speaker on): `control-selected` disc with `control-on-selected` glyph; pass the caption that undoes
 * the action ("Unmute", "Resume") as [label]. Disabled: 38%.
 */
@Composable
fun TalkdeskCallControl(
    icon: ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = TalkdeskTheme.colors
    Column(
        modifier
            .alpha(if (enabled) 1f else 0.38f)
            .clip(TalkdeskTheme.shapes.md)
            .clickable(enabled = enabled, role = Role.Switch, onClick = onClick)
            .semantics(mergeDescendants = true) {
                toggleableState = if (active) ToggleableState.On else ToggleableState.Off
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            Modifier
                .size(TalkdeskTheme.sizes.control)
                .background(if (active) colors.controlSelected else colors.surface3, TalkdeskTheme.shapes.full)
                .border(1.dp, colors.borderDivider, TalkdeskTheme.shapes.full),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (active) colors.controlOnSelected else colors.iconDefault,
                modifier = Modifier.size(TalkdeskTheme.sizes.icon),
            )
        }
        Text(label, style = callCaptionStyle(), color = colors.textDefault)
    }
}

@TalkdeskPreviews
@Composable
private fun TalkdeskCallControlPreview() = PreviewSurface {
    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        TalkdeskCallControl(TalkdeskIcons.Mic, "Mute", active = false, onClick = {})
        TalkdeskCallControl(TalkdeskIcons.MicOff, "Unmute", active = true, onClick = {})
        TalkdeskCallControl(TalkdeskIcons.Pause, "Hold", active = false, onClick = {}, enabled = false)
    }
}
