package org.talkdesk.conversation.core.designsystem.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 8dp grid with 4dp half-steps. `space16` is the default screen and card padding. */
@Immutable
data class TalkdeskSpacing(
    val space2: Dp = 2.dp,
    val space4: Dp = 4.dp,
    val space8: Dp = 8.dp,
    val space12: Dp = 12.dp,
    val space16: Dp = 16.dp,
    val space20: Dp = 20.dp,
    val space24: Dp = 24.dp,
    val space32: Dp = 32.dp,
    val space40: Dp = 40.dp,
    val space48: Dp = 48.dp,
    val space56: Dp = 56.dp,
    val space80: Dp = 80.dp,
)

/** Corner radii and the matching shapes. */
@Immutable
data class TalkdeskShapes(
    /** 2dp. Checkboxes. */
    val xs: Shape = RoundedCornerShape(2.dp),
    /** 4dp. Buttons, inputs, toasts. */
    val sm: Shape = RoundedCornerShape(4.dp),
    /** 8dp. Cards, dialogs, sheets. */
    val md: Shape = RoundedCornerShape(8.dp),
    /** 16dp. Chips. */
    val lg: Shape = RoundedCornerShape(16.dp),
    /** 24dp. */
    val xl: Shape = RoundedCornerShape(24.dp),
    /** Avatars, call buttons, pills. */
    val full: Shape = CircleShape,
)

/** Component and touch-target sizes. */
@Immutable
data class TalkdeskSizes(
    val touch: Dp = 48.dp,
    val callButton: Dp = 64.dp,
    val control: Dp = 56.dp,
    val avatar: Dp = 40.dp,
    val avatarLarge: Dp = 96.dp,
    val icon: Dp = 24.dp,
    val iconSmall: Dp = 20.dp,
    val toolbar: Dp = 56.dp,
    val bottomNav: Dp = 64.dp,
    val chip: Dp = 32.dp,
    val input: Dp = 48.dp,
    val row: Dp = 64.dp,
    val dialpadKey: Dp = 64.dp,
    val dragPill: Dp = 40.dp,
    val switchTrack: Dp = 48.dp,
    val divider: Dp = 1.dp,
    val focusRing: Dp = 2.dp,
    /** Width breakpoint at which tablet layouts start. */
    val breakpointTablet: Dp = 600.dp,
    val breakpointTabletLarge: Dp = 700.dp,
)

internal val LocalTalkdeskSpacing = staticCompositionLocalOf { TalkdeskSpacing() }
internal val LocalTalkdeskShapes = staticCompositionLocalOf { TalkdeskShapes() }
internal val LocalTalkdeskSizes = staticCompositionLocalOf { TalkdeskSizes() }
