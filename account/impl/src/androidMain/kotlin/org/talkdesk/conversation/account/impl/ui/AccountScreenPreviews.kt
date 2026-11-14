package org.talkdesk.conversation.account.impl.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.talkdesk.conversation.account.impl.R
import org.talkdesk.conversation.account.impl.presentation.AccountMock
import org.talkdesk.conversation.account.impl.presentation.AccountState
import org.talkdesk.conversation.core.designsystem.component.TalkdeskButton
import org.talkdesk.conversation.core.designsystem.component.TalkdeskButtonVariant
import org.talkdesk.conversation.core.designsystem.component.TalkdeskDialogContent
import org.talkdesk.conversation.core.designsystem.component.TalkdeskToast
import org.talkdesk.conversation.core.designsystem.component.TalkdeskToastVariant
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

// Previews of the four boards in "Account · Android" (01 Account and status, 02 Status changed,
// 03 Status change failed, 04 Log out), plus the loading state.

private const val PreviewVersion = "6.9.0"

private fun state(selectedId: String = AccountMock.AVAILABLE, loading: Boolean = false) = AccountState(
    profile = AccountMock.profile,
    statusGroups = AccountMock.statusGroups,
    selectedStatusId = selectedId,
    isLoading = loading,
)

@AccountPreviews
@Composable
private fun AccountPreview() = TalkdeskTheme {
    AccountScreen(state(), PreviewVersion, onIntent = {})
}

@AccountPreviews
@Composable
private fun AccountLoadingPreview() = TalkdeskTheme {
    AccountScreen(state(loading = true), PreviewVersion, onIntent = {})
}

@AccountPreviews
@Composable
private fun AccountStatusChangedPreview() = TalkdeskTheme {
    Box(Modifier.fillMaxSize()) {
        AccountScreen(state(selectedId = "away-lunch"), PreviewVersion, onIntent = {})
        TalkdeskToast(
            message = stringResource(R.string.account_status_changed, "Away · Lunch"),
            variant = TalkdeskToastVariant.Primary,
            icon = TalkdeskIcons.Check,
            actionLabel = stringResource(R.string.account_status_changed_undo),
            onAction = {},
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
        )
    }
}

@AccountPreviews
@Composable
private fun AccountStatusFailedPreview() = TalkdeskTheme {
    Box(Modifier.fillMaxSize()) {
        AccountScreen(state(), PreviewVersion, onIntent = {})
        TalkdeskToast(
            message = stringResource(R.string.account_status_failed),
            variant = TalkdeskToastVariant.Error,
            icon = TalkdeskIcons.Alert,
            actionLabel = stringResource(R.string.account_status_failed_retry),
            onAction = {},
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
        )
    }
}

/** The dialog window itself cannot render in a preview, so its content is drawn over a scrim. */
@AccountPreviews
@Composable
private fun AccountLogOutPreview() = TalkdeskTheme {
    Box(Modifier.fillMaxSize()) {
        AccountScreen(state(selectedId = "away-training"), PreviewVersion, onIntent = {}, scrollState = ScrollState(Int.MAX_VALUE))
        Box(Modifier.fillMaxSize().background(TalkdeskTheme.colors.scrim), contentAlignment = Alignment.Center) {
            TalkdeskDialogContent(
                title = stringResource(R.string.account_logout_dialog_title),
                text = stringResource(R.string.account_logout_dialog_body),
                dismissButton = {
                    TalkdeskButton(stringResource(R.string.account_logout_dialog_cancel), {}, variant = TalkdeskButtonVariant.Ghost)
                },
                confirmButton = {
                    TalkdeskButton(stringResource(R.string.account_log_out), {}, variant = TalkdeskButtonVariant.Ghost)
                },
            )
        }
    }
}
