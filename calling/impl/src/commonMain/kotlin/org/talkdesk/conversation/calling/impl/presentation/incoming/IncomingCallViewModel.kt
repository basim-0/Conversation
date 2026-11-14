package org.talkdesk.conversation.calling.impl.presentation.incoming

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import org.talkdesk.conversation.calling.impl.presentation.CallContact

/** State holder of the incoming call screen. UI only for now (TEMPORARY): the caller is mock data. */
class IncomingCallViewModel : ViewModel() {

    private val _state = MutableStateFlow(
        IncomingCallState(
            caller = CallContact(name = "Maria Silva", initials = "MS", phoneType = null, number = "+1 555 0142"),
            lineName = "Support line",
        ),
    )
    val state: StateFlow<IncomingCallState> = _state.asStateFlow()

    private val _effects = Channel<IncomingCallEffect>(Channel.BUFFERED)
    val effects: Flow<IncomingCallEffect> = _effects.receiveAsFlow()

    fun onIntent(intent: IncomingCallIntent) {
        when (intent) {
            IncomingCallIntent.Accept -> _effects.trySend(IncomingCallEffect.Accepted)
            IncomingCallIntent.Decline -> _effects.trySend(IncomingCallEffect.Declined)
        }
    }
}
