package org.talkdesk.conversation.calling.impl.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

/**
 * A ViewModel store that lives as long as the calling entry is on screen, so every call, dialer and
 * incoming screen starts from a fresh ViewModel instead of reusing the Activity's. It is cleared when
 * the entry leaves composition, which includes a configuration change.
 *
 * TODO: replace with `lifecycle-viewmodel-navigation3`'s entry decorator (survives rotation) once that
 * dependency is approved; the root `NavDisplay` has no ViewModel decorator today.
 */
@Composable
internal fun rememberEntryViewModelStoreOwner(): ViewModelStoreOwner {
    val owner = remember {
        object : ViewModelStoreOwner {
            override val viewModelStore = ViewModelStore()
        }
    }
    DisposableEffect(owner) { onDispose { owner.viewModelStore.clear() } }
    return owner
}
