package org.talkdesk.conversation.core.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape

/** The [SharedTransitionScope] of the app's root navigation. Null outside it (previews, tests). */
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

/** The [AnimatedVisibilityScope] of the root navigation entry being composed. Null outside it. */
val LocalSharedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

/** Keys that tie a container in one feature to its counterpart in another, without the features knowing each other. */
object SharedTransitionKeys {
    /** The Activity "New call" button and the dialer screen it opens. */
    const val NewCall = "new-call-container"
}

private const val ContainerDurationMillis = 300
private const val FadeDurationMillis = 150

/**
 * Marks this node as one side of a container transform: the node with the same [key] on the other
 * screen grows from / shrinks back into it. Does nothing when there is no root navigation around, so
 * previews and screens shown on their own keep working. Place it first in the modifier chain, before
 * background, clip and size.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedContainer(key: String, shape: Shape = RectangleShape): Modifier {
    val transitionScope = LocalSharedTransitionScope.current ?: return this
    val visibilityScope = LocalSharedVisibilityScope.current ?: return this
    return with(transitionScope) {
        this@sharedContainer.sharedBounds(
            sharedContentState = rememberSharedContentState(key),
            animatedVisibilityScope = visibilityScope,
            enter = fadeIn(tween(FadeDurationMillis, delayMillis = FadeDurationMillis / 2)),
            exit = fadeOut(tween(FadeDurationMillis)),
            boundsTransform = { _, _ -> tween(ContainerDurationMillis, easing = FastOutSlowInEasing) },
            resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
            clipInOverlayDuringTransition = OverlayClip(shape),
        )
    }
}
