package org.talkdesk.conversation.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.talkdesk.conversation.account.impl.ui.navigation.accountEntries
import org.talkdesk.conversation.activity.impl.ui.navigation.activityEntries
import org.talkdesk.conversation.auth.api.AuthSession
import org.talkdesk.conversation.auth.api.LoginRoute
import org.talkdesk.conversation.auth.api.SessionState
import org.talkdesk.conversation.auth.impl.ui.login.LoginFlow
import org.talkdesk.conversation.calling.api.CallRoute
import org.talkdesk.conversation.calling.api.NewCallRoute
import org.talkdesk.conversation.calling.impl.ui.navigation.callingEntries
import org.talkdesk.conversation.core.ui.LocalSharedTransitionScope
import org.talkdesk.conversation.core.ui.LocalSharedVisibilityScope
import org.talkdesk.conversation.home.api.HomeRoute
import org.talkdesk.conversation.home.impl.ui.HomeScreen

/** Root navigation: the back stack follows the auth session. */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun ConversationRoot(
    redirects: Flow<String>,
    authSession: AuthSession = koinInject()
) {

    val scope = rememberCoroutineScope()
    val session by authSession.state.collectAsStateWithLifecycle()
    val backStack = remember { mutableStateListOf<NavKey>(LoginRoute) }
    // Each feature contributes its screens here; the home shell shows them as tabs.
    val homeEntries = remember {
        entryProvider<NavKey> {
            activityEntries(onNewCall = { backStack.add(NewCallRoute) })
            accountEntries(onSignOut = { scope.launch { authSession.logout() } })
        }
    }

    LaunchedEffect(session) {
        val target: NavKey = if (session is SessionState.SignedIn) HomeRoute else LoginRoute
        if (backStack.lastOrNull() != target) {
            backStack.clear()
            backStack.add(target)
        }
    }

    // The root NavDisplay shares one transition scope so features can link containers across screens
    // (the Activity "New call" button grows into the dialer) without knowing each other.
    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            NavDisplay(
                backStack = backStack,
                entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator(), sharedVisibilityDecorator),
                sharedTransitionScope = this,
                // Fade through: the old screen leaves quickly, the new one fades in. It also lets the
                // shared container (button -> dialer) morph without a competing slide.
                transitionSpec = { rootFadeThrough() },
                popTransitionSpec = { rootFadeThrough() },
                predictivePopTransitionSpec = { rootFadeThrough() },
                entryProvider = entryProvider {
                    entry<LoginRoute> { LoginFlow(incomingRedirects = redirects) }
                    entry<HomeRoute> { HomeScreen(tabEntries = homeEntries) }
                    // Dialer, call and incoming screens are full-screen destinations above the home shell.
                    callingEntries(
                        onClose = { if (backStack.size > 1) backStack.removeLastOrNull() },
                        onCallStarted = {
                            backStack.removeLastOrNull()
                            backStack.add(CallRoute)
                        },
                    )
                },
            )
        }
    }
}

/** Gives every root entry the [AnimatedVisibilityScope] of its transition, which shared containers need. */
private val sharedVisibilityDecorator = NavEntryDecorator<NavKey>(decorate = { entry ->
    CompositionLocalProvider(LocalSharedVisibilityScope provides LocalNavAnimatedContentScope.current) {
        entry.Content()
    }
})

private fun rootFadeThrough(): ContentTransform =
    fadeIn(tween(durationMillis = 210, delayMillis = 90, easing = LinearOutSlowInEasing)) togetherWith
        fadeOut(tween(durationMillis = 90, easing = FastOutLinearInEasing))
