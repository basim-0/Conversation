package org.talkdesk.conversation.calling.impl.ui.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import org.talkdesk.conversation.calling.api.CallRoute
import org.talkdesk.conversation.calling.api.IncomingCallRoute
import org.talkdesk.conversation.calling.api.NewCallRoute

/**
 * The Calling feature's navigation entries: the dialer, the call screen and the incoming call. They are
 * full-screen destinations above the home shell.
 *
 * @param onClose leave the current calling screen (back, call ended or minimized, incoming declined).
 * @param onCallStarted replace the dialer or incoming screen with the call screen.
 */
fun EntryProviderScope<NavKey>.callingEntries(
    onClose: () -> Unit,
    onCallStarted: () -> Unit,
) {
    entry<NewCallRoute> { DialerFlow(onBack = onClose, onCallStarted = onCallStarted) }
    entry<CallRoute> { CallFlow(onClose = onClose) }
    entry<IncomingCallRoute> { IncomingCallFlow(onAccepted = onCallStarted, onDeclined = onClose) }
}
