package org.talkdesk.conversation.auth.impl.ui.login.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import org.talkdesk.conversation.auth.impl.R
import org.talkdesk.conversation.auth.impl.ui.login.components.AppMark
import org.talkdesk.conversation.auth.impl.ui.login.components.LoginPreviews
import org.talkdesk.conversation.core.designsystem.component.TalkdeskSpinner
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

/**
 * "Signing you in…" (Android board A08): shown after the SSO redirect returns to the app, while the
 * session is created.
 */
@Composable
fun SigningInScreen(accountName: String, modifier: Modifier = Modifier) {
    val colors = TalkdeskTheme.colors
    val signingIn = stringResource(R.string.auth_signing_in)
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bgWindow),
        verticalArrangement = Arrangement.spacedBy(TalkdeskTheme.spacing.space24, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppMark(large = true)
        TalkdeskSpinner(Modifier.semantics { contentDescription = signingIn })
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(TalkdeskTheme.spacing.space4),
        ) {
            Text(signingIn, style = TalkdeskTheme.typography.bodyStrong, color = colors.textDefault)
            Text(
                text = stringResource(R.string.auth_account_host, accountName),
                style = TalkdeskTheme.typography.caption,
                color = colors.textDescription,
            )
        }
    }
}

@LoginPreviews
@Composable
private fun SigningInPreview() = TalkdeskTheme {
    SigningInScreen(accountName = "acme")
}
