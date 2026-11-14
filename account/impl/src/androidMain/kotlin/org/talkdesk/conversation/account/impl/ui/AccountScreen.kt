package org.talkdesk.conversation.account.impl.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.talkdesk.conversation.account.impl.R
import org.talkdesk.conversation.account.impl.presentation.AccountDialog
import org.talkdesk.conversation.account.impl.presentation.AccountIntent
import org.talkdesk.conversation.account.impl.presentation.AccountState
import org.talkdesk.conversation.account.impl.ui.components.ProfileCard
import org.talkdesk.conversation.account.impl.ui.components.StatusList
import org.talkdesk.conversation.account.impl.ui.components.StatusListSkeleton
import org.talkdesk.conversation.core.designsystem.component.TalkdeskButton
import org.talkdesk.conversation.core.designsystem.component.TalkdeskButtonVariant
import org.talkdesk.conversation.core.designsystem.component.TalkdeskDialog
import org.talkdesk.conversation.core.designsystem.component.TalkdeskIconButton
import org.talkdesk.conversation.core.designsystem.component.TalkdeskLargeToolbar
import org.talkdesk.conversation.core.designsystem.component.TalkdeskSnackbarHost
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

/**
 * Account tab: the agent's profile card with the current presence, the status list grouped by presence
 * type, then Log out and the app version below it. Stateless: renders [state] and reports taps as
 * [AccountIntent]s. Toasts (status changed / failed) are shown by the caller through [snackbarHostState].
 */
@Composable
fun AccountScreen(
    state: AccountState,
    appVersion: String,
    onIntent: (AccountIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    scrollState: ScrollState = rememberScrollState(),
) {
    val colors = TalkdeskTheme.colors
    Box(modifier.fillMaxSize().background(colors.bgWindow)) {
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars)) {
            TalkdeskLargeToolbar(title = stringResource(R.string.account_title)) {
                // TODO: the design system icon set has no "feedback" glyph (bubble with "!"); Message is the closest.
                TalkdeskIconButton(
                    icon = TalkdeskIcons.Message,
                    contentDescription = stringResource(R.string.account_feedback),
                    onClick = { onIntent(AccountIntent.SendFeedback) },
                )
            }
            Column(Modifier.weight(1f).verticalScroll(scrollState)) {
                ProfileCard(
                    profile = state.profile,
                    presence = state.selectedStatus?.presence,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 8.dp),
                )
                Text(
                    text = stringResource(R.string.account_status_header).uppercase(),
                    style = TalkdeskTheme.typography.overline,
                    color = colors.textDescription,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 4.dp),
                )
                if (state.isLoading) {
                    StatusListSkeleton()
                } else {
                    StatusList(
                        groups = state.statusGroups,
                        selectedStatusId = state.selectedStatusId,
                        enabled = !state.isChangingStatus,
                        onSelect = { onIntent(AccountIntent.SelectStatus(it)) },
                    )
                }
                HorizontalDivider(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), color = colors.borderDivider)
                LogOutRow(onClick = { onIntent(AccountIntent.LogOutClicked) })
                AppVersion(appVersion, Modifier.padding(top = 24.dp, bottom = 24.dp))
            }
        }
        TalkdeskSnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
        AccountDialogs(state.dialog, onIntent)
    }
}

@Composable
private fun LogOutRow(onClick: () -> Unit) {
    val colors = TalkdeskTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(TalkdeskIcons.Logout, contentDescription = null, tint = colors.textDefault, modifier = Modifier.size(24.dp))
        Text(stringResource(R.string.account_log_out), style = TalkdeskTheme.typography.body, color = colors.textDefault)
    }
}

@Composable
private fun AppVersion(version: String, modifier: Modifier = Modifier) {
    val colors = TalkdeskTheme.colors
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            stringResource(R.string.account_app_name),
            style = TalkdeskTheme.typography.caption.copy(fontWeight = FontWeight.Medium),
            color = colors.textDescription,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(R.string.account_app_version, version),
            style = TalkdeskTheme.typography.caption,
            color = colors.textDescription,
            textAlign = TextAlign.Center,
        )
    }
}

/** Log out confirmation, or the "end your call first" notice. Both are design-system dialogs. */
@Composable
private fun AccountDialogs(dialog: AccountDialog?, onIntent: (AccountIntent) -> Unit) {
    when (dialog) {
        AccountDialog.ConfirmLogOut -> TalkdeskDialog(
            onDismissRequest = { onIntent(AccountIntent.DismissDialog) },
            title = stringResource(R.string.account_logout_dialog_title),
            text = stringResource(R.string.account_logout_dialog_body),
            dismissButton = {
                TalkdeskButton(
                    stringResource(R.string.account_logout_dialog_cancel),
                    onClick = { onIntent(AccountIntent.DismissDialog) },
                    variant = TalkdeskButtonVariant.Ghost,
                )
            },
            confirmButton = {
                TalkdeskButton(
                    stringResource(R.string.account_log_out),
                    onClick = { onIntent(AccountIntent.ConfirmLogOut) },
                    variant = TalkdeskButtonVariant.Ghost,
                )
            },
        )

        AccountDialog.EndCallFirst -> TalkdeskDialog(
            onDismissRequest = { onIntent(AccountIntent.DismissDialog) },
            title = stringResource(R.string.account_logout_in_call_title),
            confirmButton = {
                TalkdeskButton(
                    stringResource(R.string.account_logout_in_call_ok),
                    onClick = { onIntent(AccountIntent.DismissDialog) },
                    variant = TalkdeskButtonVariant.Ghost,
                )
            },
        )

        null -> Unit
    }
}
