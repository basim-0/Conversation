package org.talkdesk.conversation.calling.impl.presentation.country

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.talkdesk.conversation.calling.impl.data.CountryRepository

/**
 * State holder of the country list. Searches by name or dial code (case-insensitive, "contains"), as the
 * legacy app did, and remembers the picked country through the [CountryRepository].
 */
class CountryPickerViewModel internal constructor(private val countries: CountryRepository) : ViewModel() {

    private val query = MutableStateFlow("")

    private val _state = MutableStateFlow(CountryPickerState())
    val state: StateFlow<CountryPickerState> = _state.asStateFlow()

    private val _effects = Channel<CountryPickerEffect>(Channel.BUFFERED)
    val effects: Flow<CountryPickerEffect> = _effects.receiveAsFlow()

    init {
        combine(query, countries.selected) { query, selected ->
            CountryPickerState(
                query = query,
                countries = countries.countries.filter { it.matches(query) },
                selectedIsoCode = selected.isoCode,
                isLoading = false,
            )
        }.onEach { state -> _state.value = state }.launchIn(viewModelScope)
    }

    fun onIntent(intent: CountryPickerIntent) {
        when (intent) {
            is CountryPickerIntent.QueryChanged -> {
                query.value = intent.query
                _state.update { it.copy(query = intent.query) }
            }
            is CountryPickerIntent.Select -> viewModelScope.launch {
                countries.select(intent.isoCode)
                _effects.send(CountryPickerEffect.Close)
            }
            CountryPickerIntent.Back -> _effects.trySend(CountryPickerEffect.Close)
        }
    }

    private fun org.talkdesk.conversation.calling.impl.domain.model.DialCountry.matches(query: String) =
        query.isEmpty() || name.contains(query, ignoreCase = true) || dialCode.contains(query, ignoreCase = true)
}
