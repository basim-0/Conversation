package org.talkdesk.conversation.core.push

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/** Bridge from the platform's token callback (e.g. `onNewToken`) to [PushTokenProvider.tokenChanges]. */
class PushTokenRelay {
    private val changes = MutableSharedFlow<String>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val tokenChanges: Flow<String> get() = changes

    fun onNewToken(token: String) {
        changes.tryEmit(token)
    }
}
