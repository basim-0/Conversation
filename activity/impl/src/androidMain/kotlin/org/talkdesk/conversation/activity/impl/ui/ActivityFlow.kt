package org.talkdesk.conversation.activity.impl.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * Entry point of the Activity feature, shown by [activityEntries]. UI only for now: it keeps a local
 * mock state and applies intents with [ActivityMock.reduce].
 * TODO: replace with a ViewModel (`collectAsStateWithLifecycle` + `viewModel::onIntent`) when data lands.
 */
@Composable
internal fun ActivityFlow(modifier: Modifier = Modifier) {
    var state by remember { mutableStateOf(ActivityMock.state()) }
    ActivityScreen(state = state, onIntent = { state = ActivityMock.reduce(state, it) }, modifier = modifier)
}
