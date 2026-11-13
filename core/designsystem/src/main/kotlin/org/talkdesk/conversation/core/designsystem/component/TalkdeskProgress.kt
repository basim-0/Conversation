package org.talkdesk.conversation.core.designsystem.component

import android.provider.Settings
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

/**
 * Determinate 4dp bar (`ta-progress`) on `track` with a `track-fill` fill. [threshold] switches the
 * fill to `status-error` when a time limit is near.
 */
@Composable
fun TalkdeskLinearProgress(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    threshold: Boolean = false,
) {
    val colors = TalkdeskTheme.colors
    val fill = if (threshold) colors.statusError else colors.trackFill
    val shape = TalkdeskTheme.shapes.full
    Box(
        modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(shape)
            .background(colors.track)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(progress().coerceIn(0f, 1f), 0f..1f) }
            .drawWithContent {
                val p = progress().coerceIn(0f, 1f)
                drawRoundRect(fill, size = size.copy(width = size.width * p), cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2))
            },
    )
}

/**
 * Indeterminate 4dp bar: a 40% segment sweeping across (1200ms). Becomes a static half-opacity bar
 * when the system has animations turned off.
 */
@Composable
fun TalkdeskLinearProgress(modifier: Modifier = Modifier) {
    val colors = TalkdeskTheme.colors
    val shape = TalkdeskTheme.shapes.full
    val reduceMotion = rememberReduceMotion()
    val base = modifier
        .fillMaxWidth()
        .height(4.dp)
        .clip(shape)
        .background(colors.track)
        .semantics { progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate }

    if (reduceMotion) {
        Box(base) { Box(Modifier.fillMaxWidth().fillMaxHeight().background(colors.trackFill.copy(alpha = 0.5f))) }
        return
    }
    val transition = rememberInfiniteTransition(label = "indeterminate")
    val position by transition.animateFloat(
        initialValue = -0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)), RepeatMode.Restart),
        label = "position",
    )
    Box(
        base.drawWithContent {
            drawRoundRect(
                colors.trackFill,
                topLeft = Offset(size.width * position, 0f),
                size = size.copy(width = size.width * 0.4f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2),
            )
        },
    )
}

/**
 * 24dp circular spinner (`ta-spinner`): 3dp `track` ring with a rotating `track-fill` quarter
 * (800ms). Static when the system has animations turned off.
 */
@Composable
fun TalkdeskSpinner(modifier: Modifier = Modifier) {
    val colors = TalkdeskTheme.colors
    val reduceMotion = rememberReduceMotion()
    val rotation = if (reduceMotion) {
        0f
    } else {
        val transition = rememberInfiniteTransition(label = "spinner")
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing)),
            label = "rotation",
        ).value
    }
    Canvas(
        modifier
            .size(24.dp)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate },
    ) {
        val stroke = 3.dp.toPx()
        val inset = stroke / 2
        val arcSize = size.copy(width = size.width - stroke, height = size.height - stroke)
        drawArc(colors.track, 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
        drawArc(colors.trackFill, rotation - 135f, 90f, false, Offset(inset, inset), arcSize, style = Stroke(stroke, cap = StrokeCap.Butt))
    }
}

/**
 * True when the user turned animations off (Settings › Accessibility › Remove animations). Use it to
 * replace movement with a short crossfade or a static frame.
 */
@Composable
fun rememberReduceMotion(): Boolean {
    val resolver = LocalContext.current.contentResolver
    return remember(resolver) {
        Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}

@TalkdeskPreviews
@Composable
private fun TalkdeskProgressPreview() = PreviewSurface {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TalkdeskLinearProgress(progress = { 0.6f })
        TalkdeskLinearProgress(progress = { 0.85f }, threshold = true)
        TalkdeskLinearProgress()
        TalkdeskSpinner()
    }
}
