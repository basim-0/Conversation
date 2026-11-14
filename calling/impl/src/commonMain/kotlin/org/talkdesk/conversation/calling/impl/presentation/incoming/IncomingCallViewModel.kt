package org.talkdesk.conversation.calling.impl.presentation.incoming

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import org.talkdesk.conversation.calling.impl.domain.CallManager
import org.talkdesk.conversation.calling.impl.presentation.toContact

/** State holder of the incoming call screen: shows the call that is ringing and answers or declines it. */
class IncomingCallViewModel internal constructor(private val calls: CallManager) : ViewModel() {

    private val _state = MutableStateFlow(IncomingCallState())
    val state: StateFlow<IncomingCallState> = _state.asStateFlow()

    private val _effects = Channel<IncomingCallEffect>(Channel.BUFFERED)
    val effects: Flow<IncomingCallEffect> = _effects.receiveAsFlow()

    private var ringingCallId: String? = null

    init {
        calls.calls.onEach { list ->
            val call = list.firstOrNull { it.isRinging } ?: return@onEach
            ringingCallId = call.id
            _state.value = IncomingCallState(caller = call.toContact(), lineName = call.lineName.orEmpty())
        }.launchIn(viewModelScope)
    }

    fun onIntent(intent: IncomingCallIntent) {
        val id = ringingCallId ?: return
        when (intent) {
            IncomingCallIntent.Accept -> {
                calls.accept(id)
                _effects.trySend(IncomingCallEffect.Accepted)
            }
            IncomingCallIntent.Decline -> {
                calls.reject(id)
                _effects.trySend(IncomingCallEffect.Declined)
            }
        }
    }
}
