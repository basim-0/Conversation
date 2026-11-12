package org.talkdesk.conversation.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

/** Elevation levels. Light theme draws shadows; dark theme relies on the surface ladder instead. */
enum class TalkdeskElevation { Raised, Overlay }

private val RaisedLight = Shadow(radius = 3.dp, color = Color(0xFF1D2123).copy(alpha = 0.12f), offset = DpOffset(0.dp, 1.dp))
private val OverlayLight = Shadow(radius = 16.dp, color = Color(0xFF1D2123).copy(alpha = 0.24f), offset = DpOffset(0.dp, 6.dp))
private val OverlayDark = Shadow(radius = 16.dp, color = Color.Black.copy(alpha = 0.48f), offset = DpOffset(0.dp, 6.dp))

/**
 * Draws the design-system shadow (`shadow-raised` / `shadow-overlay`) behind the content, using the
 * current theme. `shadow-raised` is `none` in dark mode.
 */
@Composable
fun Modifier.talkdeskShadow(elevation: TalkdeskElevation, shape: Shape): Modifier {
    val dark = TalkdeskTheme.colors.isDark
    val shadow = when (elevation) {
        TalkdeskElevation.Raised -> if (dark) null else RaisedLight
        TalkdeskElevation.Overlay -> if (dark) OverlayDark else OverlayLight
    }
    return if (shadow == null) this else this.dropShadow(shape, shadow)
}
