package org.talkdesk.conversation.activity.impl.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.talkdesk.conversation.activity.impl.presentation.ActivityViewModel
import org.talkdesk.conversation.activity.impl.ui.ActivityScreen

/** Entry point of the Activity feature, shown by [activityEntries]: renders the ViewModel's state and sends it intents. */
@Composable
internal fun ActivityFlow(modifier: Modifier = Modifier, viewModel: ActivityViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ActivityScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}
