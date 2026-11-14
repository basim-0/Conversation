package org.talkdesk.conversation.calling.impl.ui.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import org.talkdesk.conversation.calling.api.CountryPickerRoute
import org.talkdesk.conversation.calling.api.NewCallRoute

/**
 * The Calling feature's navigation entries. Only the dialer is a navigation destination: the call screens
 * are hosted by their own activity (see `CallActivity`) so they can show over the lock screen.
 *
 * @param onClose leave the current screen (back).
 * @param onCallStarted the call exists and its screen is open: close the dialer.
 * @param onPickCountry open the country list.
 */
fun EntryProviderScope<NavKey>.callingEntries(
    onClose: () -> Unit,
    onCallStarted: () -> Unit,
    onPickCountry: () -> Unit,
) {
    entry<NewCallRoute> { DialerFlow(onBack = onClose, onCallStarted = onCallStarted, onPickCountry = onPickCountry) }
    entry<CountryPickerRoute> { CountryPickerFlow(onClose = onClose) }
}
