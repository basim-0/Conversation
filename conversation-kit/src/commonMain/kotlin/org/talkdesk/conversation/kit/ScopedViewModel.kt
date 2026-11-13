package org.talkdesk.conversation.kit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.CreationExtras
import kotlin.reflect.KClass

/**
 * A ViewModel together with the [ViewModelStore] that owns it. iOS does not clear ViewModels
 * automatically: the SwiftUI owner of a screen must call [clear] when it goes away (`deinit`),
 * which cancels the ViewModel's `viewModelScope`.
 */
class ScopedViewModel<VM : ViewModel> internal constructor(
    val viewModel: VM,
    private val store: ViewModelStore,
) {
    fun clear() = store.clear()
}

/** Creates a [ViewModel] with [create] inside its own [ViewModelStore]. */
internal inline fun <reified VM : ViewModel> scopedViewModel(crossinline create: () -> VM): ScopedViewModel<VM> {
    val store = ViewModelStore()
    val factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T = create() as T
    }
    return ScopedViewModel(ViewModelProvider.create(store, factory)[VM::class], store)
}
