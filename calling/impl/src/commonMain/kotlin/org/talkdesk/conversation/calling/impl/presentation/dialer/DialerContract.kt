package org.talkdesk.conversation.calling.impl.presentation.dialer

import org.talkdesk.conversation.calling.impl.presentation.CallContact

/**
 * Data contract of the dialer ("New call"). [DialerViewModel] owns a [DialerState] in one `StateFlow`,
 * receives [DialerIntent]s through `onIntent` and sends one-shot [DialerEffect]s.
 */
data class DialerState(
    /** The number the call is placed from. Read-only. */
    val callerId: CallerId = CallerId(label = "Default number", number = "+1 555 0100"),
    val country: DialCountry = DialCountry(isoCode = "US", name = "United States", dialCode = "+1"),
    /** Digits typed so far, unformatted. */
    val digits: String = "",
    /** [digits] formatted for display as the agent types. */
    val formattedNumber: String = "",
    /** A contact whose number matches what was typed. */
    val suggestedContact: CallContact? = null,
    /** The last call attempt was rejected as an invalid number; cleared by the next key. */
    val isNumberInvalid: Boolean = false,
) {
    /** Call is disabled until there is a number. */
    val canCall: Boolean get() = digits.isNotEmpty()
}

data class CallerId(val label: String, val number: String)

data class DialCountry(val isoCode: String, val name: String, val dialCode: String)

sealed interface DialerIntent {
    data class KeyPressed(val key: Char) : DialerIntent
    data object DeleteDigit : DialerIntent

    /** Long-press on delete: remove the whole number. */
    data object ClearNumber : DialerIntent

    /** Long-press on the readout: the clipboard text. */
    data class PasteNumber(val text: String) : DialerIntent
    data object PickCountry : DialerIntent
    data object PickContact : DialerIntent
    data object UseSuggestedContact : DialerIntent
    data object Call : DialerIntent
    data object Back : DialerIntent
}

sealed interface DialerEffect {
    /** Place the call to [number] and open the call screen. */
    data class StartCall(val number: String) : DialerEffect
    data object NavigateBack : DialerEffect
}
