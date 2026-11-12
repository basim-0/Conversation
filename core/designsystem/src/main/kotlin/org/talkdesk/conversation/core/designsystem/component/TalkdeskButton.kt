package org.talkdesk.conversation.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskColors
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

/** Button variants from the design system. Use only one [Primary] per screen. */
enum class TalkdeskButtonVariant {
    /** `action-primary` fill. The single main action of a screen. */
    Primary,

    /** `surface-1` with a `border-control` outline. Alternatives to the primary action. */
    Secondary,

    /** Borderless, `text-brand`. Low-emphasis actions in cards, toolbars and dialogs. */
    Ghost,

    /** Borderless, `text-info`. */
    GhostInfo,

    /** Borderless, `text-error`. */
    GhostDanger,

    /** `action-reject` fill. Only to confirm an irreversible action in a dialog. */
    Destructive,
}

enum class TalkdeskButtonSize { Default, Small }

/**
 * Text button for every non-call action. Height 48 (small: 36), radius `radius-sm`, `body-strong`
 * label (sentence case, no full stop). Shows a 2dp `border-focus` ring at a 2dp offset when focused.
 */
@Composable
fun TalkdeskButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: TalkdeskButtonVariant = TalkdeskButtonVariant.Primary,
    size: TalkdeskButtonSize = TalkdeskButtonSize.Default,
    enabled: Boolean = true,
    fullWidth: Boolean = false,
    leadingIcon: ImageVector? = null,
    interactionSource: MutableInteractionSource? = null,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val focused by source.collectIsFocusedAsState()
    val colors = TalkdeskTheme.colors
    val style = buttonStyle(variant, colors, enabled, pressed)
    val shape = TalkdeskTheme.shapes.sm
    val focusColor = colors.borderFocus
    val ghostLike = variant in GhostVariants

    val horizontalPadding = when {
        size == TalkdeskButtonSize.Small || ghostLike -> 12.dp
        else -> 20.dp
    }
    val textStyle = TalkdeskTheme.typography.bodyStrong.let {
        if (size == TalkdeskButtonSize.Small) it.copy(fontSize = 14.sp) else it
    }

    Row(
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .focusRing(focused, focusColor, ringWidth = 2.dp, offset = 2.dp, cornerRadius = 4.dp)
            .defaultMinSize(minHeight = if (size == TalkdeskButtonSize.Small) 36.dp else 48.dp)
            .clip(shape)
            .background(style.container, shape)
            .then(if (style.border != null) Modifier.border(style.border, shape) else Modifier)
            .clickable(
                interactionSource = source,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(PaddingValues(horizontal = horizontalPadding)),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, tint = style.content, modifier = Modifier.size(20.dp))
        }
        Text(text = text, style = textStyle, color = style.content, maxLines = 1)
    }
}

/**
 * Inline text link (`ta-link`): 14/500 `text-brand`, underlined. Use for "Show more" inside cards.
 */
@Composable
fun TalkdeskLink(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = TalkdeskTheme.colors
    Text(
        text = text,
        style = TalkdeskTheme.typography.label.copy(textDecoration = TextDecoration.Underline),
        color = if (enabled) colors.textBrand else colors.textDisabled,
        modifier = modifier
            .defaultMinSize(minHeight = 24.dp)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
    )
}

private val GhostVariants = setOf(
    TalkdeskButtonVariant.Ghost,
    TalkdeskButtonVariant.GhostInfo,
    TalkdeskButtonVariant.GhostDanger,
)

private data class ButtonStyle(val container: Color, val content: Color, val border: BorderStroke?)

private fun buttonStyle(
    variant: TalkdeskButtonVariant,
    c: TalkdeskColors,
    enabled: Boolean,
    pressed: Boolean,
): ButtonStyle = when (variant) {
    TalkdeskButtonVariant.Primary -> when {
        !enabled -> ButtonStyle(c.actionPrimaryDisabled, c.textOnDisabled, null)
        pressed -> ButtonStyle(c.actionPrimaryPressed, c.textOnBrand, null)
        else -> ButtonStyle(c.actionPrimary, c.textOnBrand, null)
    }

    TalkdeskButtonVariant.Secondary -> when {
        !enabled -> ButtonStyle(c.surface1, c.textDisabled, BorderStroke(1.dp, c.borderDivider))
        pressed -> ButtonStyle(c.surfaceSunken, c.textDefault, BorderStroke(1.dp, c.borderControl))
        else -> ButtonStyle(c.surface1, c.textDefault, BorderStroke(1.dp, c.borderControl))
    }

    TalkdeskButtonVariant.Ghost, TalkdeskButtonVariant.GhostInfo, TalkdeskButtonVariant.GhostDanger -> {
        val content = when {
            !enabled -> c.textDisabled
            variant == TalkdeskButtonVariant.GhostInfo -> c.textInfo
            variant == TalkdeskButtonVariant.GhostDanger -> c.textError
            else -> c.textBrand
        }
        ButtonStyle(if (pressed && enabled) c.surfaceSelected else Color.Transparent, content, null)
    }

    // The design system defines no pressed/disabled colours for destructive; disabled reuses primary's.
    TalkdeskButtonVariant.Destructive -> when {
        !enabled -> ButtonStyle(c.actionPrimaryDisabled, c.textOnDisabled, null)
        else -> ButtonStyle(c.actionReject, c.textOnFill, null)
    }
}

/** Draws a rounded focus ring outside the bounds, like CSS `outline` + `outline-offset`. */
internal fun Modifier.focusRing(
    focused: Boolean,
    color: Color,
    ringWidth: Dp,
    offset: Dp,
    cornerRadius: Dp,
): Modifier = if (!focused) this else drawBehind {
    val inset = offset.toPx() + ringWidth.toPx() / 2
    drawRoundRect(
        color = color,
        topLeft = Offset(-inset, -inset),
        size = Size(size.width + inset * 2, size.height + inset * 2),
        cornerRadius = CornerRadius(cornerRadius.toPx() + inset),
        style = Stroke(width = ringWidth.toPx()),
    )
}

@TalkdeskPreviews
@Composable
private fun TalkdeskButtonPreview() = PreviewSurface {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TalkdeskButton("Log in", {})
            TalkdeskButton("Disabled", {}, enabled = false)
            TalkdeskButton("Transfer", {}, variant = TalkdeskButtonVariant.Secondary, leadingIcon = TalkdeskIcons.Transfer)
            TalkdeskButton("Disabled", {}, variant = TalkdeskButtonVariant.Secondary, enabled = false)
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TalkdeskButton("Show more", {}, variant = TalkdeskButtonVariant.Ghost)
            TalkdeskButton("Retry", {}, variant = TalkdeskButtonVariant.GhostInfo)
            TalkdeskButton("Delete", {}, variant = TalkdeskButtonVariant.GhostDanger)
            TalkdeskButton("End for all", {}, variant = TalkdeskButtonVariant.Destructive)
            TalkdeskButton("Small", {}, size = TalkdeskButtonSize.Small)
        }
        TalkdeskButton("Continue", {}, fullWidth = true)
        TalkdeskLink("Show more", {})
    }
}
