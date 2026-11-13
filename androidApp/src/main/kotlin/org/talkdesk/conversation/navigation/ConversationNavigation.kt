package org.talkdesk.conversation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.talkdesk.conversation.R
import org.talkdesk.conversation.auth.api.AuthSession
import org.talkdesk.conversation.auth.api.LoginRoute
import org.talkdesk.conversation.auth.api.SessionState
import org.talkdesk.conversation.auth.impl.ui.login.LoginFlow
import org.talkdesk.conversation.core.designsystem.component.TalkdeskButton
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

/** Where the app is: signed out (login flow) or signed in. */
private data object HomeRoute : NavKey

/** Root navigation: the back stack follows the auth session. */
@Composable
internal fun ConversationRoot(
    redirects: Flow<String>,
    authSession: AuthSession = koinInject()
) {

    val session by authSession.state.collectAsStateWithLifecycle()
    val backStack = remember { mutableStateListOf<NavKey>(LoginRoute) }

    LaunchedEffect(session) {
        val target: NavKey = if (session is SessionState.SignedIn) HomeRoute else LoginRoute
        if (backStack.lastOrNull() != target) {
            backStack.clear()
            backStack.add(target)
        }
    }

    NavDisplay(
        backStack = backStack,
        entryProvider = entryProvider {
            entry<LoginRoute> { LoginFlow(incomingRedirects = redirects) }
            entry<HomeRoute> { SignedInPlaceholder(onSignOut = authSession::logout) }
        },
    )
}

// TODO: temporary signed-in screen so sign-out can be exercised. Replace with the real home once designed.
@Composable
private fun SignedInPlaceholder(onSignOut: suspend () -> Unit) {
    val scope = rememberCoroutineScope()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TalkdeskTheme.colors.bgWindow),
        contentAlignment = Alignment.Center,
    ) {
        TalkdeskButton(
            text = stringResource(R.string.sign_out),
            onClick = { scope.launch { onSignOut() } },
        )
    }
}
