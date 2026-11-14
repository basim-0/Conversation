package org.talkdesk.conversation.activity.impl.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.talkdesk.conversation.activity.impl.presentation.ActivityIntent
import org.talkdesk.conversation.activity.impl.presentation.ActivityViewModel
import org.talkdesk.conversation.activity.impl.ui.ActivityScreen

/** Entry point of the Activity feature, shown by [activityEntries]: renders the ViewModel's state and sends it intents (except New call, which goes to [onNewCall]). */
@Composable
internal fun ActivityFlow(
    onNewCall: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActivityViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnNewCall by rememberUpdatedState(onNewCall)
    ActivityScreen(
        state = state,
        // Starting a call is another feature: navigation handles it, the ViewModel never sees it.
        onIntent = { intent -> if (intent == ActivityIntent.NewCall) currentOnNewCall() else viewModel.onIntent(intent) },
        modifier = modifier,
    )
}
