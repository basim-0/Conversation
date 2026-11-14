package org.talkdesk.conversation.calling.impl.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.talkdesk.conversation.calling.impl.R
import org.talkdesk.conversation.calling.impl.presentation.call.CallEffect
import org.talkdesk.conversation.calling.impl.presentation.call.CallViewModel
import org.talkdesk.conversation.calling.impl.presentation.country.CountryPickerEffect
import org.talkdesk.conversation.calling.impl.presentation.country.CountryPickerViewModel
import org.talkdesk.conversation.calling.impl.presentation.dialer.CallFailure
import org.talkdesk.conversation.calling.impl.presentation.dialer.DialerEffect
import org.talkdesk.conversation.calling.impl.presentation.dialer.DialerIntent
import org.talkdesk.conversation.calling.impl.presentation.dialer.DialerViewModel
import org.talkdesk.conversation.calling.impl.presentation.incoming.IncomingCallEffect
import org.talkdesk.conversation.calling.impl.presentation.incoming.IncomingCallIntent
import org.talkdesk.conversation.calling.impl.presentation.incoming.IncomingCallViewModel
import org.talkdesk.conversation.calling.impl.ui.CallActivity
import org.talkdesk.conversation.calling.impl.ui.call.CallScreen
import org.talkdesk.conversation.calling.impl.ui.country.CountryPickerScreen
import org.talkdesk.conversation.calling.impl.ui.dialer.DialerScreen
import org.talkdesk.conversation.calling.impl.ui.incoming.IncomingCallScreen
import org.talkdesk.conversation.calling.impl.ui.rememberMicrophoneGate
import org.talkdesk.conversation.core.designsystem.component.TalkdeskSnackbarHost
import org.talkdesk.conversation.core.designsystem.component.TalkdeskToastVariant
import org.talkdesk.conversation.core.designsystem.component.showTalkdeskToast
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme
import org.talkdesk.conversation.core.ui.CollectEffects
import org.talkdesk.conversation.core.ui.SharedTransitionKeys
import org.talkdesk.conversation.core.ui.sharedContainer

/**
 * Entry point of the dialer: renders its ViewModel's state, sends intents, and navigates on its effects.
 * Asks for the microphone before placing a call; once the call exists the call screens open in
 * [CallActivity] and the dialer closes.
 */
@Composable
internal fun DialerFlow(
    onBack: () -> Unit,
    onCallStarted: () -> Unit,
    onPickCountry: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DialerViewModel = koinViewModel(viewModelStoreOwner = rememberEntryViewModelStoreOwner()),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val alertIcon = TalkdeskIcons.Alert
    val currentOnBack by rememberUpdatedState(onBack)
    val currentOnCallStarted by rememberUpdatedState(onCallStarted)
    val currentOnPickCountry by rememberUpdatedState(onPickCountry)

    fun showError(message: String) {
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            snackbar.showTalkdeskToast(message = message, variant = TalkdeskToastVariant.Error, icon = alertIcon)
        }
    }

    val withMicrophone = rememberMicrophoneGate(onDenied = { showError(context.getString(R.string.calling_microphone_required)) })

    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            DialerEffect.NavigateBack -> currentOnBack()
            DialerEffect.OpenCountryPicker -> currentOnPickCountry()
            DialerEffect.CallStarted -> {
                context.startActivity(CallActivity.intent(context))
                currentOnCallStarted()
            }
            is DialerEffect.CallFailed -> showError(
                context.getString(
                    when (effect.failure) {
                        CallFailure.Busy -> R.string.calling_call_busy
                        CallFailure.Offline -> R.string.calling_offline
                        CallFailure.Other -> R.string.calling_call_failed
                    },
                ),
            )
        }
    }
    // The dialer grows out of the Activity "New call" button (and shrinks back into it on close).
    Box(modifier.sharedContainer(SharedTransitionKeys.NewCall, TalkdeskTheme.shapes.lg)) {
        DialerScreen(
            state = state,
            onIntent = { intent ->
                if (intent == DialerIntent.Call) withMicrophone { viewModel.onIntent(intent) } else viewModel.onIntent(intent)
            },
            modifier = Modifier.fillMaxSize(),
        )
        TalkdeskSnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }
}

/** Entry point of the call screen: renders its ViewModel's state; minimizing closes the call activity. */
@Composable
internal fun CallFlow(
    onMinimize: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CallViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnMinimize by rememberUpdatedState(onMinimize)
    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            // The host closes the activity when the call is over; "Call ended · m:ss" comes with the post-call work.
            is CallEffect.CallEnded -> Unit
            CallEffect.Minimized -> currentOnMinimize()
        }
    }
    CallScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}

/** Entry point of the incoming call. Answering asks for the microphone first through [withMicrophone]. */
@Composable
internal fun IncomingCallFlow(
    withMicrophone: (action: () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: IncomingCallViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // The host switches screens from the call state, so the effects need no handling here.
    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            IncomingCallEffect.Accepted, IncomingCallEffect.Declined -> Unit
        }
    }
    IncomingCallScreen(
        state = state,
        onIntent = { intent ->
            if (intent == IncomingCallIntent.Accept) withMicrophone { viewModel.onIntent(intent) } else viewModel.onIntent(intent)
        },
        modifier = modifier,
    )
}

/** Entry point of the country list: renders its ViewModel's state and closes once a country is picked. */
@Composable
internal fun CountryPickerFlow(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CountryPickerViewModel = koinViewModel(viewModelStoreOwner = rememberEntryViewModelStoreOwner()),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnClose by rememberUpdatedState(onClose)
    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            CountryPickerEffect.Close -> currentOnClose()
        }
    }
    CountryPickerScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}
