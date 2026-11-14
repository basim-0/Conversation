package org.talkdesk.conversation.calling.impl.presentation.country

import org.talkdesk.conversation.calling.impl.domain.model.DialCountry

/**
 * Data contract of the country list opened from the dialer. [CountryPickerViewModel] owns a
 * [CountryPickerState] in one `StateFlow`, receives [CountryPickerIntent]s and sends one-shot
 * [CountryPickerEffect]s.
 */
data class CountryPickerState(
    val query: String = "",
    /** Countries whose name or dial code contains [query], sorted by name. */
    val countries: List<DialCountry> = emptyList(),
    /** The country picked now, highlighted in the list. */
    val selectedIsoCode: String? = null,
    /** The list is still loading the first time. */
    val isLoading: Boolean = true,
) {
    val isEmptyResult: Boolean get() = !isLoading && countries.isEmpty()
}

sealed interface CountryPickerIntent {
    data class QueryChanged(val query: String) : CountryPickerIntent
    data class Select(val isoCode: String) : CountryPickerIntent
    data object Back : CountryPickerIntent
}

sealed interface CountryPickerEffect {
    /** Leave the list. */
    data object Close : CountryPickerEffect
}
