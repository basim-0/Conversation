package org.talkdesk.conversation.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow

/**
 * Collects one-shot [effects] (navigation, snackbars, opening a browser...) only while the lifecycle is
 * at least [minActiveState]. Collection stops when the screen goes to the background and restarts when
 * it comes back, so nothing is handled while the UI cannot show it.
 *
 * Effects emitted meanwhile are not lost as long as the producer buffers them (e.g. a buffered `Channel`
 * exposed with `receiveAsFlow()`).
 *
 * [onEffect] may suspend. Launch a coroutine from it for long-running work (such as an indefinite
 * snackbar), otherwise later effects wait for it. The latest lambda is always used without restarting
 * the collection.
 */
@Composable
fun <T> CollectEffects(
    effects: Flow<T>,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    onEffect: suspend (T) -> Unit,
) {
    val currentOnEffect by rememberUpdatedState(onEffect)
    LaunchedEffect(effects, lifecycleOwner, minActiveState) {
        lifecycleOwner.repeatOnLifecycle(minActiveState) {
            effects.collect { currentOnEffect(it) }
        }
    }
}
