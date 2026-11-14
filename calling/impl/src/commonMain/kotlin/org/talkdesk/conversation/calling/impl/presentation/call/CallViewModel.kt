package org.talkdesk.conversation.calling.impl.presentation.call

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.talkdesk.conversation.calling.impl.presentation.CallContact

/**
 * State holder of the call screen. UI only for now (TEMPORARY): the contact is mock data and the call
 * "connects" after [CONNECT_DELAY_MS] with a ticking timer, so the screens can be seen working. Real
 * call state will come from the calling domain.
 */
class CallViewModel : ViewModel() {

    private val _state = MutableStateFlow(CallState(phase = CallPhase.Calling, contact = MockContact))
    val state: StateFlow<CallState> = _state.asStateFlow()

    private val _effects = Channel<CallEffect>(Channel.BUFFERED)
    val effects: Flow<CallEffect> = _effects.receiveAsFlow()

    private var timer: Job? = null

    init {
        viewModelScope.launch {
            delay(CONNECT_DELAY_MS)
            _state.update { it.copy(phase = CallPhase.OnCall, quality = CallQuality.Good) }
            startTimer()
        }
    }

    fun onIntent(intent: CallIntent) {
        when (intent) {
            CallIntent.ToggleMute -> _state.update { it.copy(isMuted = !it.isMuted) }
            CallIntent.ToggleHold -> _state.update {
                when (it.phase) {
                    CallPhase.OnCall -> it.copy(phase = CallPhase.OnHold, quality = null)
                    CallPhase.OnHold -> it.copy(phase = CallPhase.OnCall, quality = CallQuality.Good)
                    CallPhase.Calling -> it
                }
            }
            CallIntent.ToggleSpeaker -> _state.update { it.copy(isSpeakerOn = !it.isSpeakerOn) }
            CallIntent.ShowKeypad -> _state.update { if (it.canUseConnectedControls) it.copy(isKeypadOpen = true) else it }
            CallIntent.HideKeypad -> _state.update { it.copy(isKeypadOpen = false) }
            is CallIntent.SendDigit -> _state.update { it.copy(dtmfDigits = it.dtmfDigits + intent.digit) }
            CallIntent.EndCall -> {
                timer?.cancel()
                _effects.trySend(CallEffect.CallEnded(_state.value.elapsedSeconds))
            }
            CallIntent.Minimize -> _effects.trySend(CallEffect.Minimized)
        }
    }

    private fun startTimer() {
        timer?.cancel()
        timer = viewModelScope.launch {
            while (true) {
                delay(1_000)
                _state.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }
        }
    }

    private companion object {
        const val CONNECT_DELAY_MS = 2_500L
        val MockContact = CallContact(name = "Maria Silva", initials = "MS", phoneType = "Mobile", number = "+1 555 0142")
    }
}
