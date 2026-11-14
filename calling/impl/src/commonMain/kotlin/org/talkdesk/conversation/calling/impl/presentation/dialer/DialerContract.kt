package org.talkdesk.conversation.calling.impl.presentation.dialer

import org.talkdesk.conversation.calling.impl.domain.model.DialCountry
import org.talkdesk.conversation.calling.impl.presentation.CallContact

/**
 * Data contract of the dialer ("New call"). [DialerViewModel] owns a [DialerState] in one `StateFlow`,
 * receives [DialerIntent]s through `onIntent` and sends one-shot [DialerEffect]s.
 */
data class DialerState(
    /** The number the call is placed from. Read-only. Null until the server answers. */
    val callerId: CallerId? = null,
    /** The country the number is dialled from: the picked one, or the one a `+` number belongs to. */
    val country: DialCountry = DialCountry(isoCode = "US", name = "United States", dialCode = "+1"),
    /** The country can be picked only while the number is not in international (`+`) form. */
    val isCountrySelectionEnabled: Boolean = true,
    /** Digits typed so far, unformatted. */
    val digits: String = "",
    /** [digits] formatted for display as the agent types. */
    val formattedNumber: String = "",
    /** A contact whose number matches what was typed. */
    val suggestedContact: CallContact? = null,
    /** The last call attempt was rejected as an invalid number; cleared by the next key. */
    val isNumberInvalid: Boolean = false,
    /** The call is being prepared: Call is disabled until it starts or fails. */
    val isStartingCall: Boolean = false,
) {
    /** Call is disabled until there is a number. */
    val canCall: Boolean get() = digits.isNotEmpty() && !isStartingCall
}

/** [label] is the caller ID's display name, when it has one. */
data class CallerId(val label: String?, val number: String)

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

/** Why a call could not be started. The UI maps it to text. */
enum class CallFailure { Busy, Offline, Other }

sealed interface DialerEffect {
    /** The call was created: open the call screen. */
    data object CallStarted : DialerEffect
    data class CallFailed(val failure: CallFailure) : DialerEffect
    data object NavigateBack : DialerEffect

    /** Open the country list. */
    data object OpenCountryPicker : DialerEffect
}
