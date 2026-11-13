package org.talkdesk.conversation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.talkdesk.conversation.activity.impl.ui.navigation.activityEntries
import org.talkdesk.conversation.auth.api.AuthSession
import org.talkdesk.conversation.auth.api.LoginRoute
import org.talkdesk.conversation.auth.api.SessionState
import org.talkdesk.conversation.auth.impl.ui.login.LoginFlow
import org.talkdesk.conversation.home.api.HomeRoute
import org.talkdesk.conversation.home.impl.ui.HomeScreen

/** Root navigation: the back stack follows the auth session. */
@Composable
internal fun ConversationRoot(
    redirects: Flow<String>,
    authSession: AuthSession = koinInject()
) {

    val scope = rememberCoroutineScope()
    // Each feature contributes its screens here; the home shell shows them as tabs.
    val homeEntries = remember { entryProvider<NavKey> { activityEntries() } }
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
            entry<HomeRoute> { HomeScreen(
                    onSignOut = { scope.launch { authSession.logout() } },
                    tabEntries = homeEntries,
                ) }
        },
    )
}
