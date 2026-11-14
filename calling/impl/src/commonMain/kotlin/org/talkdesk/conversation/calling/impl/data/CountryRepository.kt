package org.talkdesk.conversation.calling.impl.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.talkdesk.conversation.calling.impl.domain.model.DialCountry
import org.talkdesk.conversation.calling.impl.domain.port.PhoneNumberParser

/**
 * The countries the dialer offers and the one the agent picked. Like the legacy app: every region the phone
 * number library supports, sorted by English name, and the choice is remembered between launches.
 */
internal class CountryRepository(
    private val parser: PhoneNumberParser,
    private val store: DataStore<Preferences>,
) {
    /** All countries, sorted by name. */
    val countries: List<DialCountry> by lazy { parser.countries().sortedBy { it.name } }

    private val byIsoCode: Map<String, DialCountry> by lazy { countries.associateBy { it.isoCode } }

    /** The picked country, or [DEFAULT_ISO_CODE] until the agent picks one. */
    val selected: Flow<DialCountry> = store.data.map { preferences -> resolve(preferences[SELECTED_KEY]) }

    suspend fun select(isoCode: String) {
        if (isoCode in byIsoCode) store.edit { it[SELECTED_KEY] = isoCode }
    }

    /** The country of a number typed in international form (`+...`), or null while it cannot be told. */
    fun detect(number: String): DialCountry? = parser.regionOf(number)?.let(byIsoCode::get)

    private fun resolve(isoCode: String?): DialCountry =
        byIsoCode[isoCode] ?: byIsoCode.getValue(DEFAULT_ISO_CODE)

    private companion object {
        const val DEFAULT_ISO_CODE = "US"
        val SELECTED_KEY = stringPreferencesKey("selected_country_iso2")
    }
}
