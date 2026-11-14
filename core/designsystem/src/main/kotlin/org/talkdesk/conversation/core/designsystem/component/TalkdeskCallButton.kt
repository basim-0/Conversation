package org.talkdesk.conversation.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

/** Call lifecycle buttons from the design system. */
enum class TalkdeskCallButtonKind {
    /** `action-call`. Accept an incoming call or start one. */
    Call,

    /** `action-reject`. Decline an incoming call. */
    Reject,

    /** `action-end`. Hang up. */
    End,
}

/**
 * Circular 64dp call button (`ta-callbtn`) with a `text-on-fill` icon. Accept is on the right and
 * reject/end on the left, 48dp apart. Pressed Call uses `action-call-pressed`; disabled is 38% and not
 * interactive. Prefer [TalkdeskCallButtonWithCaption] on call screens: the caption is required there so
 * call actions never rely on colour alone.
 */
@Composable
fun TalkdeskCallButton(
    kind: TalkdeskCallButtonKind,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector = if (kind == TalkdeskCallButtonKind.Call) TalkdeskIcons.Phone else TalkdeskIcons.PhoneEnd,
) {
    val colors = TalkdeskTheme.colors
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val container = when (kind) {
        TalkdeskCallButtonKind.Call -> if (pressed) colors.actionCallPressed else colors.actionCall
        TalkdeskCallButtonKind.Reject -> colors.actionReject
        TalkdeskCallButtonKind.End -> colors.actionEnd
    }
    Box(
        modifier
            .size(TalkdeskTheme.sizes.callButton)
            .alpha(if (enabled) 1f else 0.38f)
            .clip(TalkdeskTheme.shapes.full)
            .background(container, TalkdeskTheme.shapes.full)
            .clickable(
                interactionSource = source,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = colors.textOnFill, modifier = Modifier.size(TalkdeskTheme.sizes.icon))
    }
}

/** [TalkdeskCallButton] with its visible caption underneath (`ta-callbtn-wrap`, 13/18 Medium, 8dp gap). */
@Composable
fun TalkdeskCallButtonWithCaption(
    kind: TalkdeskCallButtonKind,
    caption: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector = if (kind == TalkdeskCallButtonKind.Call) TalkdeskIcons.Phone else TalkdeskIcons.PhoneEnd,
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TalkdeskCallButton(kind, contentDescription = caption, onClick = onClick, enabled = enabled, icon = icon)
        Text(caption, style = callCaptionStyle(), color = TalkdeskTheme.colors.textDefault)
    }
}

@TalkdeskPreviews
@Composable
private fun TalkdeskCallButtonPreview() = PreviewSurface {
    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        TalkdeskCallButtonWithCaption(TalkdeskCallButtonKind.Call, "Accept", {})
        TalkdeskCallButtonWithCaption(TalkdeskCallButtonKind.Reject, "Decline", {})
        TalkdeskCallButtonWithCaption(TalkdeskCallButtonKind.End, "End", {})
        TalkdeskCallButtonWithCaption(TalkdeskCallButtonKind.Call, "Disabled", {}, enabled = false)
    }
}
