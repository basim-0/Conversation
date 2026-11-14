package org.talkdesk.conversation.calling.impl.presentation.dialer

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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.talkdesk.conversation.calling.impl.data.CallerIdRepository
import org.talkdesk.conversation.calling.impl.data.CountryRepository
import org.talkdesk.conversation.calling.impl.domain.CallBusyException
import org.talkdesk.conversation.calling.impl.domain.CallManager
import org.talkdesk.conversation.calling.impl.domain.model.DialCountry
import org.talkdesk.conversation.calling.impl.domain.port.PhoneNumberParser
import org.talkdesk.conversation.core.network.utils.AppError
import org.talkdesk.conversation.core.network.utils.appErrorOrNull

/**
 * State holder of the dialer. Number formatting and validation come from the platform's
 * [PhoneNumberParser]; the call itself is placed by the [CallManager]. The country is the one the agent
 * picked (remembered between launches); a number typed with `+` takes the country it belongs to and locks
 * the picker until the number is cleared, like the legacy app. Picking a contact has no destination yet.
 */
class DialerViewModel internal constructor(
    private val calls: CallManager,
    callerIds: CallerIdRepository,
    private val countries: CountryRepository,
    private val parser: PhoneNumberParser,
) : ViewModel() {

    private val _state = MutableStateFlow(DialerState())
    val state: StateFlow<DialerState> = _state.asStateFlow()

    private val _effects = Channel<DialerEffect>(Channel.BUFFERED)
    val effects: Flow<DialerEffect> = _effects.receiveAsFlow()

    /** The picked country, and the country detected from a `+` number (kept while the number is incomplete). */
    private var pickedCountry: DialCountry = _state.value.country
    private var detectedCountry: DialCountry? = null

    init {
        countries.selected.onEach { country ->
            pickedCountry = country
            _state.update { refresh(it, it.digits) }
        }.launchIn(viewModelScope)
        callerIds.observe().onEach { callerId ->
            _state.update { it.copy(callerId = callerId?.let { id -> CallerId(id.displayName, id.number) }) }
        }.launchIn(viewModelScope)
        viewModelScope.launch { callerIds.refresh() }
    }

    fun onIntent(intent: DialerIntent) {
        when (intent) {
            is DialerIntent.KeyPressed -> updateDigits { it + intent.key }
            DialerIntent.DeleteDigit -> updateDigits { it.dropLast(1) }
            DialerIntent.ClearNumber -> updateDigits { "" }
            is DialerIntent.PasteNumber -> updateDigits { intent.text.filter(::isDialChar) }
            // There is no contact search yet, so nothing is ever suggested.
            DialerIntent.UseSuggestedContact -> Unit
            DialerIntent.Call -> call()
            DialerIntent.Back -> _effects.trySend(DialerEffect.NavigateBack)
            DialerIntent.PickCountry -> if (_state.value.isCountrySelectionEnabled) {
                _effects.trySend(DialerEffect.OpenCountryPicker)
            }
            // No contact picker is designed yet.
            DialerIntent.PickContact -> Unit
        }
    }

    private fun call() {
        val state = _state.value
        if (!state.canCall) return
        val number = parser.toE164(state.digits, state.country.isoCode)
        if (number == null) {
            _state.update { it.copy(isNumberInvalid = true) }
            return
        }
        _state.update { it.copy(isStartingCall = true) }
        viewModelScope.launch {
            val result = calls.placeCall(number)
            _state.update { it.copy(isStartingCall = false) }
            if (result.isSuccess) {
                _effects.send(DialerEffect.CallStarted)
            } else {
                _effects.send(DialerEffect.CallFailed(result.failureReason()))
            }
        }
    }

    private fun Result<Unit>.failureReason(): CallFailure = when {
        exceptionOrNull().let { it is CallBusyException || it?.cause is CallBusyException } -> CallFailure.Busy
        appErrorOrNull() is AppError.Offline -> CallFailure.Offline
        else -> CallFailure.Other
    }

    private fun updateDigits(transform: (String) -> String) {
        _state.update { current ->
            val limit = if (transform(current.digits).startsWith("+")) MAX_DIGITS + 1 else MAX_DIGITS
            refresh(current, transform(current.digits).take(limit))
        }
    }

    /** Recomputes everything that follows from the typed [digits] and the picked country. */
    private fun refresh(current: DialerState, digits: String): DialerState {
        val international = digits.startsWith("+")
        if (!international) detectedCountry = null
        // "+" alone, or a prefix too short to tell: keep the last country that was told.
        if (international) countries.detect(digits)?.let { detectedCountry = it }
        val country = if (international) detectedCountry ?: pickedCountry else pickedCountry
        return current.copy(
            digits = digits,
            country = country,
            isCountrySelectionEnabled = !international,
            formattedNumber = if (digits.matches(DialableNumber)) parser.formatPartial(digits, country.isoCode) else digits,
            isNumberInvalid = false,
        )
    }

    private fun isDialChar(c: Char) = c.isDigit() || c == '*' || c == '#' || c == '+'

    private companion object {
        const val MAX_DIGITS = 15
        val DialableNumber = Regex("\\+?\\d+")
    }
}
