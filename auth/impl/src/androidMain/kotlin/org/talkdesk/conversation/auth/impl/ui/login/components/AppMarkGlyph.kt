package org.talkdesk.conversation.auth.impl.ui.login.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.scale

/**
 * The app mark's glyph from the login design: a chat bubble (`M4 5h16v11H9l-5 4z`, 2-unit round
 * stroke) with three typing dots at y = 10.5, on a 24-unit grid. The dots are drawn separately so the
 * splash can pulse them. Placeholder until the official Talkdesk Conversations icon exists.
 */
internal object AppMarkGlyph {
    const val GRID = 24f
    const val STROKE = 2f
    val DotCentersX = floatArrayOf(8f, 12f, 16f)
    const val DOT_Y = 10.5f

    /** Dot radius in the animated splash (`r = 1.25`). */
    const val DOT_RADIUS_SPLASH = 1.25f

    /** Dot radius of the static mark (a zero-length round-capped segment, half the stroke). */
    const val DOT_RADIUS_STATIC = 1f

    val Bubble: Path = Path().apply {
        moveTo(4f, 5f)
        lineTo(20f, 5f)
        lineTo(20f, 16f)
        lineTo(9f, 16f)
        lineTo(4f, 20f)
        close()
    }
}

/**
 * Draws the glyph with its top-left at [topLeft], [side] px wide.
 *
 * @param dotAlpha opacity of each dot (0–1).
 * @param dotLift how far each dot rises, in grid units.
 * @param dotRadius dot radius in grid units.
 */
internal fun DrawScope.drawAppMarkGlyph(
    topLeft: Offset,
    side: Float,
    color: Color,
    dotAlpha: FloatArray = FullDots,
    dotLift: FloatArray = NoLift,
    dotRadius: Float = AppMarkGlyph.DOT_RADIUS_STATIC,
) {
    val unit = side / AppMarkGlyph.GRID
    translate(topLeft.x, topLeft.y) {
        scale(unit, unit, pivot = Offset.Zero) {
            drawPath(
                path = AppMarkGlyph.Bubble,
                color = color,
                style = Stroke(width = AppMarkGlyph.STROKE, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
            for (i in 0..2) {
                drawCircle(
                    color = color,
                    radius = dotRadius,
                    center = Offset(AppMarkGlyph.DotCentersX[i], AppMarkGlyph.DOT_Y - dotLift[i]),
                    alpha = dotAlpha[i].coerceIn(0f, 1f),
                )
            }
        }
    }
}

private val FullDots = floatArrayOf(1f, 1f, 1f)
private val NoLift = floatArrayOf(0f, 0f, 0f)
