package org.talkdesk.conversation.auth.impl.ui.login.splashscreen

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.core.Ease
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.Easing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.util.lerp
import androidx.core.view.WindowCompat
import org.talkdesk.conversation.auth.impl.R
import org.talkdesk.conversation.auth.impl.ui.login.components.AppMarkGlyph
import org.talkdesk.conversation.auth.impl.ui.login.screens.AccountNameScreen
import org.talkdesk.conversation.auth.impl.ui.login.components.LoginPreviews
import org.talkdesk.conversation.auth.impl.ui.login.components.drawAppMarkGlyph
import org.talkdesk.conversation.core.designsystem.component.rememberReduceMotion
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme
import kotlin.math.max

/**
 * Animated splash that hands off to the first screen ("Splash · Android and iOS" board).
 *
 * The first frame matches the system splash (`action-primary` with the 96dp white mark, dots at 35%),
 * so the system splash can be removed without a visible cut. Then:
 * - 300ms: the three dots pulse in turn (300ms each, 150ms stagger) for at least two passes, and keep
 *   pulsing until [isReady]; the current pass always finishes. "Talkdesk Conversations" fades up 8dp.
 * - Handoff (500ms ease-in-out): the mark shrinks 96→40 and glides onto the login header's mark, its
 *   colours invert, and the background turns `bg-window`. The name fades out.
 * - 300ms into the handoff, [content] fades up 8dp (300ms ease-out).
 *
 * With animations off: the static launch frame (mark only) crossfades to [content] in 200ms once
 * [isReady].
 * [content] is composed from the start (hidden), so its state survives the transition.
 */
@Composable
fun SplashTransition(
    isReady: Boolean,
    modifier: Modifier = Modifier,
    onFinished: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    var finished by rememberSaveable { mutableStateOf(false) }
    val reduceMotion = rememberReduceMotion()
    val handoff = remember { AppMarkHandoff().also { it.markHidden = !finished } }
    val clock = remember { SplashClock() }
    val currentOnFinished by rememberUpdatedState(onFinished)

    LaunchedEffect(isReady) {
        if (isReady && clock.readyAt < 0) clock.readyAt = clock.elapsed
    }
    LaunchedEffect(finished) {
        if (finished) return@LaunchedEffect
        var start = -1L
        while (true) {
            withFrameMillis { now ->
                if (start < 0) start = now
                clock.elapsed = now - start
            }
            val end = clock.endTime(reduceMotion)
            if (end != null && clock.elapsed >= end) break
        }
        handoff.markHidden = false
        finished = true
        currentOnFinished()
    }

    SplashSystemBars(
        onBrand = !finished && remember(reduceMotion) {
            derivedStateOf { clock.backgroundIsBrand(reduceMotion) }
        }.value,
    )

    val colors = TalkdeskTheme.colors
    val density = LocalDensity.current
    val contentShift = with(density) { TalkdeskTheme.spacing.space8.toPx() }
    var contentCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                if (!finished) {
                    val p = if (reduceMotion) 1f else clock.handoffProgress()
                    drawRect(lerp(colors.actionPrimary, colors.bgWindow, p))
                }
            },
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    if (!finished && !reduceMotion) {
                        val q = clock.contentProgress()
                        alpha = q
                        translationY = contentShift * (1 - q)
                    } else {
                        alpha = 1f
                        translationY = 0f
                    }
                }
                // Captured inside the layer, so positions measured from here ignore the fade-up shift.
                .onGloballyPositioned { contentCoordinates = it }
                .then(if (finished) Modifier else Modifier.clearAndSetSemantics { }),
        ) {
            CompositionLocalProvider(LocalAppMarkHandoff provides handoff, content = content)
        }

        if (!finished) {
            SplashOverlay(
                clock = clock,
                reduceMotion = reduceMotion,
                targetBounds = {
                    val container = contentCoordinates
                    val mark = handoff.markCoordinates
                    if (container != null && container.isAttached && mark != null && mark.isAttached) {
                        container.localBoundingBoxOf(mark, clipBounds = false)
                    } else {
                        null
                    }
                },
            )
        }
    }
}

/** The moving mark, the name and (with animations off) the brand background. */
@Composable
private fun SplashOverlay(
    clock: SplashClock,
    reduceMotion: Boolean,
    targetBounds: () -> Rect?,
) {
    val colors = TalkdeskTheme.colors
    val sizes = TalkdeskTheme.sizes
    val spacing = TalkdeskTheme.spacing
    val appName = stringResource(R.string.auth_app_name)
    val density = LocalDensity.current
    // Name sits 24dp under the 96dp mark, which is centred on the screen.
    val nameTop = with(density) { (sizes.avatarLarge / 2 + spacing.space24).roundToPx() }
    val nameRise = with(density) { spacing.space8.toPx() }

    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = if (reduceMotion) clock.reducedOverlayAlpha() else 1f }
            .semantics { contentDescription = appName },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            if (reduceMotion) drawRect(colors.actionPrimary)
            val p = if (reduceMotion) 0f else clock.handoffProgress()
            val target = targetBounds()

            val startSide = sizes.avatarLarge.toPx()
            val endSide = sizes.avatar.toPx()
            val side = lerp(startSide, endSide, p)
            val startCenter = Offset(size.width / 2, size.height / 2)
            val center = if (target != null) lerp(startCenter, target.center, p) else startCenter
            val radius = lerp(spacing.space24.toPx(), spacing.space8.toPx(), p) // radius-xl → radius-md
            val glyphSide = lerp(spacing.space56.toPx(), sizes.icon.toPx(), p)
            val markAlpha = if (target == null) 1 - p else 1f

            drawRoundRect(
                color = lerp(colors.textOnBrand, colors.actionPrimary, p),
                topLeft = Offset(center.x - side / 2, center.y - side / 2),
                size = Size(side, side),
                cornerRadius = CornerRadius(radius),
                alpha = markAlpha,
            )
            val dotAlpha = FloatArray(3)
            val dotLift = FloatArray(3)
            for (i in 0..2) {
                val pulse = if (reduceMotion) 1f else clock.dotPulse(i)
                dotAlpha[i] = if (reduceMotion) 1f else lerp(DOT_REST_ALPHA + (1 - DOT_REST_ALPHA) * pulse, 1f, p) * markAlpha
                dotLift[i] = if (reduceMotion) 0f else DOT_LIFT * pulse
            }
            drawAppMarkGlyph(
                topLeft = Offset(center.x - glyphSide / 2, center.y - glyphSide / 2),
                side = glyphSide,
                color = lerp(colors.actionPrimary, colors.textOnBrand, p).copy(alpha = markAlpha),
                dotAlpha = dotAlpha,
                dotLift = dotLift,
                dotRadius = if (reduceMotion) {
                    AppMarkGlyph.DOT_RADIUS_STATIC
                } else {
                    lerp(AppMarkGlyph.DOT_RADIUS_SPLASH, AppMarkGlyph.DOT_RADIUS_STATIC, p)
                },
            )
        }

        Text(
            text = appName,
            style = TalkdeskTheme.typography.title,
            color = colors.textOnBrand,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints.copy(minHeight = 0))
                    layout(constraints.maxWidth, constraints.maxHeight) {
                        placeable.place(0, constraints.maxHeight / 2 + nameTop)
                    }
                }
                .graphicsLayer {
                    if (reduceMotion) {
                        // The launch frame has no name; showing it only for the 200ms fade would flash.
                        alpha = 0f
                    } else {
                        val shown = clock.nameIn()
                        alpha = shown * (1 - clock.nameOut())
                        translationY = nameRise * (1 - shown)
                    }
                },
        )
    }
}

/** Light status/navigation bar icons while the splash is `action-primary`, theme icons after. */
@Composable
private fun SplashSystemBars(onBrand: Boolean) {
    val view = LocalView.current
    val dark = isSystemInDarkTheme()
    if (view.isInEditMode) return
    SideEffect {
        val window = view.context.findActivity()?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !onBrand && !dark
            isAppearanceLightNavigationBars = !onBrand && !dark
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/** Lets the login header's mark report its position and stay hidden during the handoff. */
@Stable
internal class AppMarkHandoff {
    var markCoordinates: LayoutCoordinates? = null
    var markHidden by mutableStateOf(true)
}

internal val LocalAppMarkHandoff = staticCompositionLocalOf<AppMarkHandoff?> { null }

/** Elapsed time and the splash timeline. Every value is derived from [elapsed] (ms). */
@Stable
internal class SplashClock {
    var elapsed by mutableLongStateOf(0L)

    /** When [SplashTransition] became ready, or -1. */
    var readyAt by mutableLongStateOf(-1L)

    /** Start of the handoff: after the dot pass that is running when the app is ready (min. two passes). */
    private val handoffStart: Long?
        get() = readyAt.takeIf { it >= 0 }?.let { ready ->
            val running = if (ready < DOTS_START) 0 else ((ready - DOTS_START) / PULSE).toInt()
            val passes = max(MIN_PASSES, running + 1)
            DOTS_START + PULSE * passes + STAGGER * 2
        }

    fun endTime(reduceMotion: Boolean): Long? =
        if (reduceMotion) readyAt.takeIf { it >= 0 }?.plus(REDUCED_FADE) else handoffStart?.plus(LOGIN_DELAY + LOGIN_IN)

    fun handoffProgress(): Float = progress(handoffStart, HANDOFF, EaseInOut)

    fun contentProgress(): Float = progress(handoffStart?.plus(LOGIN_DELAY), LOGIN_IN, EaseOut)

    fun nameIn(): Float = progress(NAME_IN_START, NAME_IN, EaseOut)

    fun nameOut(): Float = progress(handoffStart, NAME_OUT, EaseOut)

    fun reducedOverlayAlpha(): Float = 1 - progress(readyAt.takeIf { it >= 0 }, REDUCED_FADE, Ease)

    fun backgroundIsBrand(reduceMotion: Boolean): Boolean =
        if (reduceMotion) reducedOverlayAlpha() > 0.5f else handoffProgress() < 0.5f

    /**
     * 0–1 lift of dot [index]: each pulse rises for 150ms and falls for 150ms (ease-in-out). Dots
     * start 150ms apart and stop after their last pass before the handoff.
     */
    fun dotPulse(index: Int): Float {
        val local = elapsed - DOTS_START - STAGGER * index
        if (local < 0) return 0f
        val pass = local / PULSE
        val passes = handoffStart?.let { (it - DOTS_START - STAGGER * 2) / PULSE }
        if (passes != null && pass >= passes) return 0f
        val f = (local % PULSE).toFloat() / PULSE
        return if (f < 0.5f) EaseInOut.transform(f * 2) else 1 - EaseInOut.transform((f - 0.5f) * 2)
    }

    private fun progress(start: Long?, duration: Long, easing: Easing): Float {
        if (start == null) return 0f
        val f = ((elapsed - start).toFloat() / duration).coerceIn(0f, 1f)
        return easing.transform(f)
    }
}

private const val DOTS_START = 300L
private const val PULSE = 300L
private const val STAGGER = 150L
private const val MIN_PASSES = 2
private const val NAME_IN_START = 400L
private const val NAME_IN = 300L
private const val NAME_OUT = 300L
private const val HANDOFF = 500L
private const val LOGIN_DELAY = 300L
private const val LOGIN_IN = 300L
private const val REDUCED_FADE = 200L

/** Dot opacity between pulses. */
private const val DOT_REST_ALPHA = 0.35f

/** How far a dot rises at the top of a pulse, in glyph grid units. */
private const val DOT_LIFT = 1.5f


/** Shows the launch frame statically; run it in interactive preview mode to watch the handoff. */
@LoginPreviews
@Composable
private fun SplashTransitionPreview() = TalkdeskTheme {
    SplashTransition(isReady = true) {
        AccountNameScreen(
            accountName = "",
            onAccountNameChange = {},
            onContinue = {},
            onHelpClick = {},
            showHelpLink = false
        )
    }
}
