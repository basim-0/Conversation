package org.talkdesk.conversation.activity.impl.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.talkdesk.conversation.core.designsystem.component.TalkdeskBanner
import org.talkdesk.conversation.core.designsystem.component.TalkdeskBannerVariant
import org.talkdesk.conversation.core.designsystem.component.TalkdeskFilterChip
import org.talkdesk.conversation.core.designsystem.component.TalkdeskSpinner
import org.talkdesk.conversation.core.designsystem.component.TalkdeskTab
import org.talkdesk.conversation.core.designsystem.component.TalkdeskTabs
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme
import org.talkdesk.conversation.activity.impl.R
import org.talkdesk.conversation.activity.impl.presentation.ActivityChannel
import org.talkdesk.conversation.activity.impl.presentation.ActivityContent
import org.talkdesk.conversation.activity.impl.presentation.ActivityDay
import org.talkdesk.conversation.activity.impl.presentation.ActivityIntent
import org.talkdesk.conversation.activity.impl.presentation.ActivityState
import org.talkdesk.conversation.activity.impl.presentation.ActivityTab
import org.talkdesk.conversation.activity.impl.ui.components.ActivityEmpty
import org.talkdesk.conversation.activity.impl.ui.components.ActivityFailed
import org.talkdesk.conversation.activity.impl.ui.components.ActivityLoading
import org.talkdesk.conversation.activity.impl.ui.components.ActivityRow
import org.talkdesk.conversation.activity.impl.ui.components.NewCallFab
import org.talkdesk.conversation.activity.impl.ui.components.labelRes

/**
 * Activity tab: calls and conversations in one timeline, grouped by day. Stateless: renders
 * [state] and reports taps as [ActivityIntent]s so a ViewModel can be attached later.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    state: ActivityState,
    onIntent: (ActivityIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TalkdeskTheme.colors
    val listState = rememberLazyListState()
    val collapseAfterPx = with(LocalDensity.current) { 24.dp.toPx() }
    val fabExpanded by remember(collapseAfterPx) {
        derivedStateOf { listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset < collapseAfterPx }
    }
    val content = state.content
    val currentOnIntent by rememberUpdatedState(onIntent)
    // Ask for the next page when the last rows come into view.
    LaunchedEffect(listState) {
        snapshotFlow {
            val info = listState.layoutInfo
            val nearEnd = (info.visibleItemsInfo.lastOrNull()?.index ?: 0) >= info.totalItemsCount - LOAD_MORE_THRESHOLD
            // Emits again when the list grows while still near the end (a short page).
            if (nearEnd) info.totalItemsCount else -1
        }.collect { total -> if (total >= 0) currentOnIntent(ActivityIntent.LoadMore) }
    }
    val showFab = content is ActivityContent.Loaded && !state.isOffline
    val showChips = state.selectedTab == ActivityTab.All && content !is ActivityContent.Loading

    Box(modifier.fillMaxSize().background(colors.bgWindow)) {
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars)) {
            Box(Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart) {
                Text(
                    stringResource(R.string.activity_title),
                    style = TalkdeskTheme.typography.h1,
                    color = colors.textDefault,
                    modifier = Modifier.semantics { heading() },
                )
            }
            TalkdeskTabs(selectedTabIndex = state.selectedTab.ordinal) {
                TalkdeskTab(
                    selected = state.selectedTab == ActivityTab.All,
                    onClick = { onIntent(ActivityIntent.SelectTab(ActivityTab.All)) },
                    label = stringResource(R.string.activity_tab_all),
                )
                TalkdeskTab(
                    selected = state.selectedTab == ActivityTab.Missed,
                    onClick = { onIntent(ActivityIntent.SelectTab(ActivityTab.Missed)) },
                    label = stringResource(R.string.activity_tab_missed),
                    badgeCount = state.missedCount.takeIf { it > 0 && state.selectedTab != ActivityTab.Missed },
                )
            }
            if (showChips) {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ActivityChannel.entries.forEach { channel ->
                        TalkdeskFilterChip(
                            label = stringResource(channel.labelRes()),
                            selected = channel in state.selectedChannels,
                            onClick = { onIntent(ActivityIntent.ToggleChannel(channel)) },
                        )
                    }
                }
            }
            if (state.isOffline) {
                TalkdeskBanner(
                    title = stringResource(R.string.activity_offline_title),
                    message = state.lastUpdatedLabel
                        ?.let { stringResource(R.string.activity_offline_message_cached, it) }
                        ?: stringResource(R.string.activity_offline_message),
                    variant = TalkdeskBannerVariant.Info,
                    icon = TalkdeskIcons.WifiOff,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
            when (content) {
                ActivityContent.Loading -> ActivityLoading()
                ActivityContent.Failed -> ActivityFailed(onIntent)
                is ActivityContent.Loaded -> if (content.sections.isEmpty()) {
                    ActivityEmpty(state.selectedTab, state.selectedChannels, onIntent)
                } else {
                    PullToRefreshBox(
                        isRefreshing = state.isRefreshing,
                        onRefresh = { onIntent(ActivityIntent.Refresh) },
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        LazyColumn(Modifier.fillMaxSize(), state = listState, contentPadding = PaddingValues(bottom = 88.dp)) {
                            content.sections.forEach { section ->
                                item(key = "header-${section.day}") { DayHeader(section.day) }
                                itemsIndexed(section.items, key = { _, item -> item.id }) { index, item ->
                                    ActivityRow(
                                        item,
                                        offline = state.isOffline,
                                        showDivider = index > 0,
                                        onIntent = onIntent
                                    )
                                }
                            }
                            if (state.isLoadingMore) {
                                item(key = "loading-more") {
                                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                        TalkdeskSpinner()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (showFab) {
            NewCallFab(
                expanded = fabExpanded,
                onClick = { onIntent(ActivityIntent.NewCall) },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            )
        }
    }
}

@Composable
private fun DayHeader(day: ActivityDay) {
    val label = when (day) {
        ActivityDay.Today -> stringResource(R.string.activity_day_today)
        ActivityDay.Yesterday -> stringResource(R.string.activity_day_yesterday)
        is ActivityDay.Date -> day.label
    }
    Text(
        text = label.uppercase(),
        style = TalkdeskTheme.typography.overline,
        color = TalkdeskTheme.colors.textDescription,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)
            .semantics { heading() },
    )
}

private const val LOAD_MORE_THRESHOLD = 3

// region Previews

@ActivityPreviews
@Composable
private fun ActivityAllPreview() = TalkdeskTheme { ActivityScreen(ActivityPreviewData.state(), {}) }

@ActivityPreviews
@Composable
private fun ActivityMissedPreview() = TalkdeskTheme { ActivityScreen(ActivityPreviewData.state(ActivityTab.Missed), {}) }

@ActivityPreviews
@Composable
private fun ActivityFilteredPreview() =
    TalkdeskTheme { ActivityScreen(ActivityPreviewData.state(channels = setOf(ActivityChannel.WhatsApp)), {}) }

@ActivityPreviews
@Composable
private fun ActivityLoadingPreview() = TalkdeskTheme { ActivityScreen(ActivityPreviewData.state(content = ActivityContent.Loading), {}) }

@ActivityPreviews
@Composable
private fun ActivityEmptyAllPreview() =
    TalkdeskTheme { ActivityScreen(ActivityPreviewData.state(content = ActivityContent.Loaded(emptyList())), {}) }

@ActivityPreviews
@Composable
private fun ActivityEmptyMissedPreview() =
    TalkdeskTheme { ActivityScreen(ActivityPreviewData.state(ActivityTab.Missed, content = ActivityContent.Loaded(emptyList())), {}) }

@ActivityPreviews
@Composable
private fun ActivityEmptyFilteredPreview() = TalkdeskTheme {
    ActivityScreen(ActivityPreviewData.state(channels = setOf(ActivityChannel.WhatsApp), content = ActivityContent.Loaded(emptyList())), {})
}

@ActivityPreviews
@Composable
private fun ActivityOfflinePreview() = TalkdeskTheme { ActivityScreen(ActivityPreviewData.state(isOffline = true), {}) }

@ActivityPreviews
@Composable
private fun ActivityFailedPreview() = TalkdeskTheme { ActivityScreen(ActivityPreviewData.state(content = ActivityContent.Failed), {}) }

@ActivityPreviews
@Composable
private fun ActivityRefreshingPreview() = TalkdeskTheme { ActivityScreen(ActivityPreviewData.state(isRefreshing = true), {}) }

@Preview(name = "Font scale 2x", widthDp = 360, heightDp = 800, fontScale = 2f, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun ActivityLargeFontPreview() = TalkdeskTheme { ActivityScreen(ActivityPreviewData.state(), {}) }

// endregion
