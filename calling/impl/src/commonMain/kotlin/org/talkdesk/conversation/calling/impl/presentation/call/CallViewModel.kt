package org.talkdesk.conversation.calling.impl.presentation.call

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.talkdesk.conversation.calling.impl.domain.CallEvent
import org.talkdesk.conversation.calling.impl.domain.CallManager
import org.talkdesk.conversation.calling.impl.domain.CallStatus
import org.talkdesk.conversation.calling.impl.domain.model.AudioEndpointType
import org.talkdesk.conversation.calling.impl.presentation.toContact
import kotlin.time.Clock

/**
 * State holder of the call screen. Shows the call in progress (the one that is not ringing), reads its
 * state from the [CallManager] and sends every control to it.
 */
class CallViewModel internal constructor(private val calls: CallManager) : ViewModel() {

    private val _state = MutableStateFlow(CallState())
    val state: StateFlow<CallState> = _state.asStateFlow()

    private val _effects = Channel<CallEffect>(Channel.BUFFERED)
    val effects: Flow<CallEffect> = _effects.receiveAsFlow()

    /** Keypad and DTMF readout: only the screen cares about them. */
    private val keypad = MutableStateFlow(KeypadState())

    private var currentCallId: String? = null

    init {
        combine(calls.calls, calls.audioRoutes, keypad, clockTicks()) { list, routes, keypad, now ->
            val call = list.firstOrNull { !it.isRinging } ?: return@combine null
            currentCallId = call.id
            CallState(
                phase = when (call.status) {
                    CallStatus.Ringing, CallStatus.Dialing, CallStatus.Connecting -> CallPhase.Calling
                    CallStatus.Active, CallStatus.Reconnecting -> CallPhase.OnCall
                    CallStatus.OnHold -> CallPhase.OnHold
                },
                contact = call.toContact(),
                elapsedSeconds = call.connectedAtMillis?.let { ((now - it) / MILLIS_PER_SECOND).toInt().coerceAtLeast(0) } ?: 0,
                isMuted = call.isMuted,
                isSpeakerOn = routes.current?.type == AudioEndpointType.Speaker,
                isKeypadOpen = keypad.isOpen,
                dtmfDigits = keypad.digits,
                quality = call.quality.takeIf { call.status == CallStatus.Active },
            )
        }.onEach { state -> if (state != null) _state.value = state }.launchIn(viewModelScope)

        calls.events.filterIsInstance<CallEvent.Ended>().onEach { event ->
            if (event.call.id == currentCallId) {
                val seconds = event.call.connectedAtMillis?.let { ((Clock.System.now().toEpochMilliseconds() - it) / MILLIS_PER_SECOND).toInt() } ?: 0
                _effects.trySend(CallEffect.CallEnded(seconds.coerceAtLeast(0)))
            }
        }.launchIn(viewModelScope)
    }

    fun onIntent(intent: CallIntent) {
        val id = currentCallId
        when (intent) {
            CallIntent.ToggleMute -> id?.let { calls.setMuted(it, !_state.value.isMuted) }
            CallIntent.ToggleHold -> id?.let { calls.setHold(it, hold = _state.value.phase == CallPhase.OnCall) }
            CallIntent.ToggleSpeaker -> calls.toggleSpeaker()
            CallIntent.ShowKeypad -> if (_state.value.canUseConnectedControls) keypad.update { it.copy(isOpen = true) }
            CallIntent.HideKeypad -> keypad.update { it.copy(isOpen = false) }
            is CallIntent.SendDigit -> id?.let {
                calls.sendDigit(it, intent.digit)
                keypad.update { state -> state.copy(digits = state.digits + intent.digit) }
            }
            CallIntent.EndCall -> id?.let(calls::hangUp)
            CallIntent.Minimize -> viewModelScope.launch { _effects.send(CallEffect.Minimized) }
        }
    }

    /** Emits the time every second so the call timer advances. */
    private fun clockTicks(): Flow<Long> = flow {
        while (true) {
            emit(Clock.System.now().toEpochMilliseconds())
            delay(MILLIS_PER_SECOND)
        }
    }

    private data class KeypadState(val isOpen: Boolean = false, val digits: String = "")

    private companion object {
        const val MILLIS_PER_SECOND = 1_000L
    }
}
