package org.talkdesk.conversation.calling.impl.presentation.dialer

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import org.talkdesk.conversation.calling.impl.presentation.CallContact

/**
 * State holder of the dialer. UI only for now: number formatting, contact matching and number
 * validation are simple stand-ins (TEMPORARY, see [MockContact] and [MIN_VALID_DIGITS]) until the
 * calling domain exists. Picking a country or a contact has no destination yet and does nothing.
 */
class DialerViewModel : ViewModel() {

    private val _state = MutableStateFlow(DialerState())
    val state: StateFlow<DialerState> = _state.asStateFlow()

    private val _effects = Channel<DialerEffect>(Channel.BUFFERED)
    val effects: Flow<DialerEffect> = _effects.receiveAsFlow()

    fun onIntent(intent: DialerIntent) {
        when (intent) {
            is DialerIntent.KeyPressed -> updateDigits { it + intent.key }
            DialerIntent.DeleteDigit -> updateDigits { it.dropLast(1) }
            DialerIntent.ClearNumber -> updateDigits { "" }
            is DialerIntent.PasteNumber -> updateDigits { intent.text.filter(::isDialChar) }
            DialerIntent.UseSuggestedContact -> _state.value.suggestedContact?.let { contact ->
                updateDigits { contact.number.filter(Char::isDigit).removePrefix("1") }
            }
            DialerIntent.Call -> call()
            DialerIntent.Back -> _effects.trySend(DialerEffect.NavigateBack)
            // No country list or contact picker is designed yet.
            DialerIntent.PickCountry, DialerIntent.PickContact -> Unit
        }
    }

    private fun call() {
        val state = _state.value
        if (!state.canCall) return
        if (state.digits.count(Char::isDigit) < MIN_VALID_DIGITS) {
            _state.update { it.copy(isNumberInvalid = true) }
        } else {
            _effects.trySend(DialerEffect.StartCall(state.country.dialCode + state.digits))
        }
    }

    private fun updateDigits(transform: (String) -> String) {
        _state.update { current ->
            val digits = transform(current.digits).take(MAX_DIGITS)
            current.copy(
                digits = digits,
                formattedNumber = formatNational(digits),
                suggestedContact = MockContact.takeIf { digits.length >= 3 && MOCK_NUMBER.startsWith(digits) },
                isNumberInvalid = false,
            )
        }
    }

    private fun isDialChar(c: Char) = c.isDigit() || c == '*' || c == '#' || c == '+'

    private companion object {
        /** TEMPORARY: stand-in for real validation. */
        const val MIN_VALID_DIGITS = 7
        const val MAX_DIGITS = 15
        const val MOCK_NUMBER = "5550142"
        val MockContact = CallContact(name = "Maria Silva", initials = "MS", phoneType = "Mobile", number = "+1 555 0142")

        /** North-American style grouping "(555) 014-2345"; anything with symbols is shown as typed. */
        fun formatNational(digits: String): String {
            if (digits.isEmpty() || !digits.all(Char::isDigit)) return digits
            return when {
                digits.length <= 3 -> digits
                digits.length <= 6 -> "(${digits.take(3)}) ${digits.drop(3)}"
                else -> "(${digits.take(3)}) ${digits.substring(3, 6)}-${digits.drop(6)}"
            }
        }
    }
}
