package org.talkdesk.conversation.core.designsystem.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

/** Call state colours (`callstate-*`). */
enum class TalkdeskCallState { Incoming, Dialing, OnCall }

@Composable
private fun TalkdeskCallState.color(): Color = when (this) {
    TalkdeskCallState.Incoming -> TalkdeskTheme.colors.callstateIncoming
    TalkdeskCallState.Dialing -> TalkdeskTheme.colors.callstateDialing
    TalkdeskCallState.OnCall -> TalkdeskTheme.colors.callstateOncall
}

/**
 * Pulse rings (`ta-rings`): three concentric discs in the call-state colour at 12/24/40% opacity around
 * [content] (the avatar). Incoming and dialing breathe (1600ms, staggered 200ms) and stay still under
 * Reduce Motion; on-call is two static 12% halos (outer and 16dp inside it), as on the design board.
 *
 * @param size outer diameter, 168dp for call screens and 192dp for the incoming screen.
 */
@Composable
fun TalkdeskCallRings(
    state: TalkdeskCallState,
    modifier: Modifier = Modifier,
    size: Dp = 168.dp,
    pulsing: Boolean = state != TalkdeskCallState.OnCall,
    content: @Composable () -> Unit,
) {
    val color = state.color()
    val reduceMotion = rememberReduceMotion()
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        if (state == TalkdeskCallState.OnCall) {
            Box(Modifier.fillMaxSize().alpha(0.12f).background(color, TalkdeskTheme.shapes.full))
            Box(Modifier.size(size - 32.dp).alpha(0.12f).background(color, TalkdeskTheme.shapes.full))
        } else {
            val transition = rememberInfiniteTransition(label = "rings")
            // Diameter of each disc as a share of [size], with its opacity: outer, middle, inner.
            listOf(Triple(1f, 0.12f, 0), Triple(0.81f, 0.24f, 200), Triple(0.67f, 0.40f, 400)).forEach { (share, opacity, delay) ->
                val breath by transition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1600),
                        repeatMode = RepeatMode.Reverse,
                        initialStartOffset = StartOffset(delay),
                    ),
                    label = "ring$delay",
                )
                val progress = if (reduceMotion || !pulsing) 0f else breath
                Box(
                    Modifier
                        .size(size * share)
                        .scale(1f + 0.06f * progress)
                        .alpha(opacity)
                        .background(color, TalkdeskTheme.shapes.full),
                )
            }
        }
        content()
    }
}

/** State label (`ta-callstate--*`): icon and word in the matching text colour, always shown next to the rings. */
@Composable
fun TalkdeskCallStateLabel(
    text: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(TalkdeskTheme.sizes.iconSmall))
        Text(text, style = TalkdeskTheme.typography.label, color = color)
    }
}

/** Call quality levels. Excellent = 3 bars, good = 2, poor = 1. */
enum class TalkdeskCallQuality { Excellent, Good, Poor }

/**
 * Call quality (`ta-quality`): three bars (6/10/14dp) plus a word. Bars are `quality-excellent`,
 * `quality-good` or `quality-bad` for the filled ones and `track` for the rest.
 */
@Composable
fun TalkdeskCallQualityIndicator(
    quality: TalkdeskCallQuality,
    label: String,
    modifier: Modifier = Modifier,
) {
    val colors = TalkdeskTheme.colors
    val (filled, fill) = when (quality) {
        TalkdeskCallQuality.Excellent -> 3 to colors.qualityExcellent
        TalkdeskCallQuality.Good -> 2 to colors.qualityGood
        TalkdeskCallQuality.Poor -> 1 to colors.qualityBad
    }
    Row(
        modifier.semantics(mergeDescendants = true) { contentDescription = label },
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            listOf(6.dp, 10.dp, 14.dp).forEachIndexed { index, barHeight ->
                Box(
                    Modifier
                        .width(4.dp)
                        .height(barHeight)
                        .background(if (index < filled) fill else colors.track, TalkdeskTheme.shapes.xs),
                )
            }
        }
        Text(label, style = TalkdeskTheme.typography.caption, color = colors.textDescription)
    }
}

@TalkdeskPreviews
@Composable
private fun TalkdeskCallStatePreview() = PreviewSurface {
    val colors = TalkdeskTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TalkdeskCallRings(TalkdeskCallState.Dialing, size = 120.dp, pulsing = false) {
                TalkdeskAvatar(initials = "MS", size = TalkdeskAvatarSize.Large)
            }
            TalkdeskCallRings(TalkdeskCallState.OnCall, size = 120.dp) {
                TalkdeskAvatar(initials = "MS", size = TalkdeskAvatarSize.Large)
            }
        }
        TalkdeskCallStateLabel("On call", TalkdeskIcons.Phone, colors.textSuccess)
        TalkdeskCallStateLabel("Calling…", TalkdeskIcons.PhoneOut, colors.textInfo)
        TalkdeskCallQualityIndicator(TalkdeskCallQuality.Excellent, "Excellent")
        TalkdeskCallQualityIndicator(TalkdeskCallQuality.Good, "Good")
        TalkdeskCallQualityIndicator(TalkdeskCallQuality.Poor, "Poor", Modifier.padding(bottom = 4.dp))
    }
}
