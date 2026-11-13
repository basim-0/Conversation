package org.talkdesk.conversation.home.impl.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import org.talkdesk.conversation.core.designsystem.component.TalkdeskBadgeValue
import org.talkdesk.conversation.core.designsystem.component.TalkdeskBottomNav
import org.talkdesk.conversation.core.designsystem.component.TalkdeskBottomNavItem
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme
import org.talkdesk.conversation.home.impl.navigation.Navigator
import org.talkdesk.conversation.home.impl.navigation.rememberNavigationState
import org.talkdesk.conversation.home.impl.presentation.HomeTab
import org.talkdesk.conversation.home.impl.ui.placeholder.TabPlaceholder
import org.talkdesk.conversation.home.impl.ui.placeholder.icon
import org.talkdesk.conversation.home.impl.ui.placeholder.labelRes
import org.talkdesk.conversation.home.impl.ui.placeholder.placeholderEntries

/**
 * The signed-in shell: Navigation 3 shows the selected tab above the bottom navigation, and each
 * tab keeps its own back stack. The screens belong to their features and arrive through
 * [tabEntries] (built by the app from each feature's entries), so Home never lists them.
 * Tabs whose feature does not exist yet show a placeholder; Account's has Sign out.
 */
@Composable
fun HomeScreen(
    onSignOut: () -> Unit,
    tabEntries: (NavKey) -> NavEntry<NavKey>,
    modifier: Modifier = Modifier,
) {
    val state = rememberNavigationState(
        startRoute = HomeTab.Activity.route,
        topLevelRoutes = HomeTab.entries.map { it.route }.toSet(),
    )
    val navigator = remember(state) { Navigator(state) }
    val currentOnSignOut by rememberUpdatedState(onSignOut)
    // Feature screens come from the app; placeholders cover tabs without a feature yet.
    val entries = remember(tabEntries) {
        entryProvider<NavKey>(fallback = tabEntries) { placeholderEntries { currentOnSignOut() } }
    }
    HomeContent(
        selectedTab = HomeTab.fromRoute(state.topLevelRoute),
        onSelectTab = { navigator.navigate(it.route) },
        modifier = modifier,
    ) {
        NavDisplay(
            entries = state.toDecoratedEntries(entries),
            onBack = { navigator.goBack() },
        )
    }
}

/** Stateless shell: [content] fills the space above the bottom navigation. */
@Composable
internal fun HomeContent(
    selectedTab: HomeTab,
    onSelectTab: (HomeTab) -> Unit,
    modifier: Modifier = Modifier,
    hasNewVoicemails: Boolean = true,
    content: @Composable () -> Unit,
) {
    Column(modifier.fillMaxSize().background(TalkdeskTheme.colors.bgWindow)) {
        Box(Modifier.weight(1f)) { content() }
        TalkdeskBottomNav {
            HomeTab.entries.forEach { tab ->
                TalkdeskBottomNavItem(
                    selected = tab == selectedTab,
                    onClick = { onSelectTab(tab) },
                    icon = tab.icon(),
                    label = stringResource(tab.labelRes()),
                    badge = TalkdeskBadgeValue.Dot.takeIf { tab == HomeTab.Voicemails && hasNewVoicemails },
                )
            }
        }
    }
}

@HomePreviews
@Composable
private fun HomeContactsPreview() = TalkdeskTheme {
    HomeContent(HomeTab.Contacts, {}) { TabPlaceholder(HomeTab.Contacts, {}) }
}

@HomePreviews
@Composable
private fun HomeAccountPreview() = TalkdeskTheme {
    HomeContent(HomeTab.Account, {}) { TabPlaceholder(HomeTab.Account, {}) }
}
