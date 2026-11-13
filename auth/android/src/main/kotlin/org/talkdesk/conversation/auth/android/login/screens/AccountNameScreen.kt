package org.talkdesk.conversation.auth.android.login.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import org.talkdesk.conversation.auth.android.R
import org.talkdesk.conversation.auth.android.login.components.LoginFormLayout
import org.talkdesk.conversation.auth.android.login.components.LoginPreviews
import org.talkdesk.conversation.core.designsystem.component.TalkdeskBanner
import org.talkdesk.conversation.core.designsystem.component.TalkdeskBannerVariant
import org.talkdesk.conversation.core.designsystem.component.TalkdeskButton
import org.talkdesk.conversation.core.designsystem.component.TalkdeskButtonSize
import org.talkdesk.conversation.core.designsystem.component.TalkdeskButtonVariant
import org.talkdesk.conversation.core.designsystem.component.TalkdeskTextField
import org.talkdesk.conversation.core.designsystem.component.TalkdeskToast
import org.talkdesk.conversation.core.designsystem.component.TalkdeskToastVariant
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

// TODO: Check if this should be part of this or it should come form the viewmodel as part of MVI
/** Message shown above the account field after coming back from SSO. */
enum class LoginBanner {
    /** Info: the SSO tab closed without a callback (A06). */
    SignInDidNotFinish,

    /** Error: SSO or session creation failed. */
    SignInFailed,
}

/**
 * Login: account name (Android board A01–A04, A06). Stateless; the caller owns every value.
 *
 * @param errorText field error ("Enter your account name." / invalid format); replaces the helper.
 * @param isOpeningSignIn A04: the button shows a spinner and "Opening sign-in…", the field is
 *   read-only and the keyboard closes.
 * @param snackbarHostState shows the offline toast (A03) with a Retry action.
 * @param requestFocus focus the field on first composition (first launch, A01).
 */
@Composable
fun AccountNameScreen(
    accountName: String,
    onAccountNameChange: (String) -> Unit,
    onContinue: () -> Unit,
    onHelpClick: () -> Unit,
    modifier: Modifier = Modifier,
    errorText: String? = null,
    banner: LoginBanner? = null,
    isOpeningSignIn: Boolean = false,
    showHelpLink: Boolean = true,
    requestFocus: Boolean = false,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val spacing = TalkdeskTheme.spacing
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(requestFocus) { if (requestFocus) focusRequester.requestFocus() }
    LaunchedEffect(isOpeningSignIn) { if (isOpeningSignIn) keyboard?.hide() }

    LoginFormLayout(
        title = stringResource(R.string.auth_sign_in_title),
        description = stringResource(R.string.auth_sign_in_description),
        modifier = modifier,
        overlay = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            ) { data ->
                OfflineToast(onRetry = data::performAction)
            }
        },
    ) {
        when (banner) {
            LoginBanner.SignInDidNotFinish -> TalkdeskBanner(
                title = stringResource(R.string.auth_banner_not_finished_title),
                message = stringResource(R.string.auth_banner_not_finished_message, accountName),
                variant = TalkdeskBannerVariant.Info,
                modifier = Modifier.padding(bottom = spacing.space16),
            )

            LoginBanner.SignInFailed -> TalkdeskBanner(
                title = stringResource(R.string.auth_banner_failed_title),
                message = stringResource(R.string.auth_banner_failed_message),
                variant = TalkdeskBannerVariant.Error,
                modifier = Modifier.padding(bottom = spacing.space16),
            )

            null -> Unit
        }

        TalkdeskTextField(
            value = accountName,
            onValueChange = onAccountNameChange,
            label = stringResource(R.string.auth_account_name_label),
            placeholder = stringResource(R.string.auth_account_name_placeholder),
            helperText = stringResource(R.string.auth_account_name_helper),
            errorText = errorText,
            readOnly = isOpeningSignIn,
            trailing = {
                Text(
                    text = stringResource(R.string.auth_account_name_suffix),
                    style = TalkdeskTheme.typography.body,
                    color = TalkdeskTheme.colors.textDescription,
                )
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Go,
            ),
            keyboardActions = KeyboardActions(onGo = { onContinue() }),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
        )

        TalkdeskButton(
            text = stringResource(if (isOpeningSignIn) R.string.auth_opening_sign_in else R.string.auth_continue),
            onClick = onContinue,
            fullWidth = true,
            loading = isOpeningSignIn,
            modifier = Modifier.padding(top = spacing.space24),
        )

        if (showHelpLink) {
            TalkdeskButton(
                text = stringResource(R.string.auth_help_link),
                onClick = onHelpClick,
                variant = TalkdeskButtonVariant.Ghost,
                size = TalkdeskButtonSize.Small,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = spacing.space8)
                    // Small ghost button is 36dp; the link needs the 48dp touch target.
                    .defaultMinSize(minHeight = TalkdeskTheme.sizes.touch),
            )
        }
    }
}

/** The A03 snackbar: error toast with the wifi-off icon and Retry, 16dp from the sides, 32dp from the bottom. */
@Composable
internal fun OfflineToast(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    val spacing = TalkdeskTheme.spacing
    TalkdeskToast(
        message = stringResource(R.string.auth_offline_message),
        variant = TalkdeskToastVariant.Error,
        icon = TalkdeskIcons.WifiOff,
        actionLabel = stringResource(R.string.auth_retry),
        onAction = onRetry,
        modifier = modifier
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(start = spacing.space16, end = spacing.space16, bottom = spacing.space32)
            .fillMaxWidth(),
    )
}

// region Previews

@LoginPreviews
@Composable
private fun AccountNameReadyPreview() = TalkdeskTheme {
    AccountNameScreen(accountName = "", onAccountNameChange = {}, onContinue = {}, onHelpClick = {}, showHelpLink = false)
}

@LoginPreviews
@Composable
private fun AccountNameInvalidPreview() = TalkdeskTheme {
    AccountNameScreen(
        accountName = "acme corp",
        onAccountNameChange = {},
        onContinue = {},
        onHelpClick = {},
        errorText = stringResource(R.string.auth_account_name_error_invalid),
        showHelpLink = false,
    )
}

@LoginPreviews
@Composable
private fun AccountNameOfflinePreview() = TalkdeskTheme {
    Box {
        AccountNameScreen(accountName = "acme", onAccountNameChange = {}, onContinue = {}, onHelpClick = {})
        // Snackbars don't show in static previews, so draw the toast where the host puts it.
        OfflineToast(onRetry = {}, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@LoginPreviews
@Composable
private fun AccountNameOpeningSignInPreview() = TalkdeskTheme {
    AccountNameScreen(accountName = "acme", onAccountNameChange = {}, onContinue = {}, onHelpClick = {}, isOpeningSignIn = true)
}

@LoginPreviews
@Composable
private fun AccountNameSignInDidNotFinishPreview() = TalkdeskTheme {
    AccountNameScreen(
        accountName = "acme",
        onAccountNameChange = {},
        onContinue = {},
        onHelpClick = {},
        banner = LoginBanner.SignInDidNotFinish,
    )
}

@LoginPreviews
@Composable
private fun AccountNameSignInFailedPreview() = TalkdeskTheme {
    AccountNameScreen(
        accountName = "acme",
        onAccountNameChange = {},
        onContinue = {},
        onHelpClick = {},
        banner = LoginBanner.SignInFailed,
    )
}

// endregion
