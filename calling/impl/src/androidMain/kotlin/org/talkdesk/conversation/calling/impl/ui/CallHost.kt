package org.talkdesk.conversation.calling.impl.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.talkdesk.conversation.calling.impl.R
import org.talkdesk.conversation.calling.impl.domain.CallEvent
import org.talkdesk.conversation.calling.impl.domain.CallManager
import org.talkdesk.conversation.calling.impl.domain.model.EndReason
import org.talkdesk.conversation.calling.impl.ui.navigation.CallFlow
import org.talkdesk.conversation.calling.impl.ui.navigation.IncomingCallFlow
import org.talkdesk.conversation.core.designsystem.component.TalkdeskSnackbarHost
import org.talkdesk.conversation.core.designsystem.component.TalkdeskToastVariant
import org.talkdesk.conversation.core.designsystem.component.showTalkdeskToast
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

/**
 * Chooses the screen from the call state: the incoming call while one rings (also during another call),
 * otherwise the call screen. Closes the activity a moment after the last call ends; a call that failed
 * shows an error toast first.
 */
@Composable
internal fun CallHost(
    answerRequests: Int,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    manager: CallManager = koinInject(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val calls by manager.calls.collectAsStateWithLifecycle()
    val hasRinging = calls.any { it.isRinging }
    val snackbar = remember { SnackbarHostState() }
    val currentOnClose by rememberUpdatedState(onClose)
    val alertIcon = TalkdeskIcons.Alert
    var failureShown by remember { mutableStateOf(false) }

    val withMicrophone = rememberMicrophoneGate(
        onDenied = {
            scope.launch {
                snackbar.showTalkdeskToast(
                    message = context.getString(R.string.calling_microphone_required),
                    variant = TalkdeskToastVariant.Error,
                    icon = alertIcon,
                )
            }
        },
    )
    val currentWithMicrophone by rememberUpdatedState(withMicrophone)

    // The notification's Answer button opens this screen and asks to answer the ringing call.
    LaunchedEffect(answerRequests) {
        if (answerRequests == 0) return@LaunchedEffect
        manager.calls.value.firstOrNull { it.isRinging }?.let { call ->
            currentWithMicrophone { manager.accept(call.id) }
        }
    }

    LaunchedEffect(manager) {
        manager.events.filterIsInstance<CallEvent.Ended>().collect { event ->
            if (event.reason == EndReason.Failed) {
                failureShown = true
                snackbar.showTalkdeskToast(
                    message = context.getString(R.string.calling_call_failed),
                    variant = TalkdeskToastVariant.Error,
                    icon = alertIcon,
                )
                currentOnClose()
            }
        }
    }

    LaunchedEffect(calls.isEmpty()) {
        if (calls.isEmpty()) {
            delay(CLOSE_DELAY_MILLIS)
            if (!failureShown) currentOnClose()
        }
    }

    Box(modifier.fillMaxSize().background(TalkdeskTheme.colors.bgWindow)) {
        if (hasRinging) {
            IncomingCallFlow(withMicrophone = withMicrophone)
        } else {
            CallFlow(onMinimize = onClose)
        }
        TalkdeskSnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }
}

private const val CLOSE_DELAY_MILLIS = 400L
