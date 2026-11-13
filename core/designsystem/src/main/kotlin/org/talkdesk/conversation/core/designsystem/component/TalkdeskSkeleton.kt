package org.talkdesk.conversation.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

/**
 * Shimmer placeholder (`ta-skel`): `skeleton-base` to `skeleton-highlight` sweep, 1200ms linear.
 * Static under Reduce Motion. Size it like the content it stands in for.
 */
@Composable
fun TalkdeskSkeleton(
    modifier: Modifier = Modifier,
    shape: Shape = TalkdeskTheme.shapes.xs,
) {
    val colors = TalkdeskTheme.colors
    val reduceMotion = rememberReduceMotion()
    val phase by if (reduceMotion) {
        remember { mutableFloatStateOf(0.5f) }
    } else {
        rememberInfiniteTransition(label = "skeleton").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart),
            label = "skeletonPhase",
        )
    }
    Box(
        modifier
            .background(colors.skeletonBase, shape)
            .drawWithCache {
                val w = size.width
                val brush = Brush.linearGradient(
                    colors = listOf(colors.skeletonBase, colors.skeletonHighlight, colors.skeletonBase),
                    start = Offset(w * (phase * 3f - 1.5f), 0f),
                    end = Offset(w * (phase * 3f - 0.5f), 0f),
                )
                onDrawWithContent {
                    drawContent()
                    drawRect(brush)
                }
            },
    )
}

@TalkdeskPreviews
@Composable
private fun TalkdeskSkeletonPreview() = PreviewSurface {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TalkdeskSkeleton(Modifier.size(40.dp), TalkdeskTheme.shapes.full)
        TalkdeskSkeleton(Modifier.fillMaxWidth(0.6f).height(14.dp))
        TalkdeskSkeleton(Modifier.fillMaxWidth(0.4f).height(12.dp))
    }
}
