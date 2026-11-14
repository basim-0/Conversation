package org.talkdesk.conversation.calling.impl.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.talkdesk.conversation.calling.impl.presentation.call.CallEffect
import org.talkdesk.conversation.calling.impl.presentation.call.CallViewModel
import org.talkdesk.conversation.calling.impl.presentation.dialer.DialerEffect
import org.talkdesk.conversation.calling.impl.presentation.dialer.DialerViewModel
import org.talkdesk.conversation.calling.impl.presentation.incoming.IncomingCallEffect
import org.talkdesk.conversation.calling.impl.presentation.incoming.IncomingCallViewModel
import org.talkdesk.conversation.calling.impl.ui.call.CallScreen
import org.talkdesk.conversation.calling.impl.ui.dialer.DialerScreen
import org.talkdesk.conversation.calling.impl.ui.incoming.IncomingCallScreen
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme
import org.talkdesk.conversation.core.ui.CollectEffects
import org.talkdesk.conversation.core.ui.SharedTransitionKeys
import org.talkdesk.conversation.core.ui.sharedContainer

/** Entry point of the dialer: renders its ViewModel's state, sends intents, and navigates on its effects. */
@Composable
internal fun DialerFlow(
    onBack: () -> Unit,
    onCallStarted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DialerViewModel = koinViewModel(viewModelStoreOwner = rememberEntryViewModelStoreOwner()),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnBack by rememberUpdatedState(onBack)
    val currentOnCallStarted by rememberUpdatedState(onCallStarted)
    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            DialerEffect.NavigateBack -> currentOnBack()
            // The number goes to the calling domain once it exists; for now only the screen changes.
            is DialerEffect.StartCall -> currentOnCallStarted()
        }
    }
    // The dialer grows out of the Activity "New call" button (and shrinks back into it on close).
    DialerScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier.sharedContainer(SharedTransitionKeys.NewCall, TalkdeskTheme.shapes.lg),
    )
}

/** Entry point of the call screen: renders its ViewModel's state and leaves when the call ends or is minimized. */
@Composable
internal fun CallFlow(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CallViewModel = koinViewModel(viewModelStoreOwner = rememberEntryViewModelStoreOwner()),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnClose by rememberUpdatedState(onClose)
    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            // "Call ended · m:ss" confirmation comes with the post-call work.
            is CallEffect.CallEnded, CallEffect.Minimized -> currentOnClose()
        }
    }
    CallScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}

/** Entry point of the incoming call: accept opens the call screen, decline closes. */
@Composable
internal fun IncomingCallFlow(
    onAccepted: () -> Unit,
    onDeclined: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: IncomingCallViewModel = koinViewModel(viewModelStoreOwner = rememberEntryViewModelStoreOwner()),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnAccepted by rememberUpdatedState(onAccepted)
    val currentOnDeclined by rememberUpdatedState(onDeclined)
    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            IncomingCallEffect.Accepted -> currentOnAccepted()
            IncomingCallEffect.Declined -> currentOnDeclined()
        }
    }
    IncomingCallScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}
