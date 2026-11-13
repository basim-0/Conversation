package org.talkdesk.conversation.auth.android.login.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.talkdesk.conversation.auth.android.R
import org.talkdesk.conversation.auth.android.login.components.LoginFormLayout
import org.talkdesk.conversation.auth.android.login.components.LoginPreviews
import org.talkdesk.conversation.auth.android.login.components.RememberedAccountRow
import org.talkdesk.conversation.core.designsystem.component.TalkdeskButton
import org.talkdesk.conversation.core.designsystem.component.TalkdeskButtonVariant
import org.talkdesk.conversation.core.designsystem.component.TalkdeskCard
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

/**
 * Login for a returning user (Android board A07): the last account that signed in, Continue, and
 * "Use a different account". Stateless; no keyboard on entry.
 */
@Composable
fun WelcomeBackScreen(
    accountName: String,
    onContinue: () -> Unit,
    onUseDifferentAccount: () -> Unit,
    modifier: Modifier = Modifier,
    isOpeningSignIn: Boolean = false,
) {
    val spacing = TalkdeskTheme.spacing
    LoginFormLayout(
        title = stringResource(R.string.auth_welcome_back_title),
        description = stringResource(R.string.auth_welcome_back_description),
        modifier = modifier,
    ) {
        TalkdeskCard(Modifier.fillMaxWidth()) {
            RememberedAccountRow(accountName)
        }
        TalkdeskButton(
            text = stringResource(if (isOpeningSignIn) R.string.auth_opening_sign_in else R.string.auth_continue),
            onClick = onContinue,
            fullWidth = true,
            loading = isOpeningSignIn,
            modifier = Modifier.padding(top = spacing.space24),
        )
        TalkdeskButton(
            text = stringResource(R.string.auth_use_different_account),
            onClick = onUseDifferentAccount,
            variant = TalkdeskButtonVariant.Ghost,
            fullWidth = true,
            modifier = Modifier.padding(top = spacing.space8),
        )
    }
}

@LoginPreviews
@Composable
private fun WelcomeBackPreview() = TalkdeskTheme {
    WelcomeBackScreen(accountName = "acme", onContinue = {}, onUseDifferentAccount = {})
}
