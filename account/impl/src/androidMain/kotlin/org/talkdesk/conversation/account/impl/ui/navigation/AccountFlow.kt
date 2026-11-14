package org.talkdesk.conversation.account.impl.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.talkdesk.conversation.account.impl.R
import org.talkdesk.conversation.account.impl.presentation.AccountEffect
import org.talkdesk.conversation.account.impl.presentation.AccountIntent
import org.talkdesk.conversation.account.impl.presentation.AccountViewModel
import org.talkdesk.conversation.account.impl.ui.AccountScreen
import org.talkdesk.conversation.account.impl.ui.displayName
import org.talkdesk.conversation.core.designsystem.component.TalkdeskToastVariant
import org.talkdesk.conversation.core.designsystem.component.showTalkdeskToast
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons
import org.talkdesk.conversation.core.ui.CollectEffects

/**
 * Entry point of the Account feature, shown by [accountEntries]: renders the ViewModel's state, sends
 * it intents and turns its effects into toasts and sign-out.
 */
@Composable
internal fun AccountFlow(
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentOnSignOut by rememberUpdatedState(onSignOut)
    val checkIcon = TalkdeskIcons.Check
    val alertIcon = TalkdeskIcons.Alert
    val appVersion = remember(context) {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull().orEmpty()
    }

    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            is AccountEffect.StatusChanged -> scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                val result = snackbarHostState.showTalkdeskToast(
                    message = context.getString(R.string.account_status_changed, effect.status.displayName(context)),
                    variant = TalkdeskToastVariant.Primary,
                    actionLabel = context.getString(R.string.account_status_changed_undo),
                    icon = checkIcon,
                    duration = SnackbarDuration.Short,
                )
                if (result == SnackbarResult.ActionPerformed) viewModel.onIntent(AccountIntent.UndoStatusChange)
            }

            AccountEffect.StatusChangeFailed -> scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                val result = snackbarHostState.showTalkdeskToast(
                    message = context.getString(R.string.account_status_failed),
                    variant = TalkdeskToastVariant.Error,
                    actionLabel = context.getString(R.string.account_status_failed_retry),
                    icon = alertIcon,
                    duration = SnackbarDuration.Indefinite,
                )
                if (result == SnackbarResult.ActionPerformed) viewModel.onIntent(AccountIntent.RetryStatusChange)
            }

            AccountEffect.SignOut -> currentOnSignOut()
        }
    }

    AccountScreen(
        state = state,
        appVersion = appVersion,
        onIntent = viewModel::onIntent,
        modifier = modifier.fillMaxSize(),
        snackbarHostState = snackbarHostState,
    )
}
