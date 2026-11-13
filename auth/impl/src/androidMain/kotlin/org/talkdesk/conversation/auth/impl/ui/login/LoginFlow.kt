package org.talkdesk.conversation.auth.impl.ui.login

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.browser.auth.AuthTabIntent
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.talkdesk.conversation.auth.impl.R
import org.talkdesk.conversation.auth.impl.ui.login.screens.AccountNameScreen
import org.talkdesk.conversation.auth.impl.ui.login.screens.SigningInScreen
import org.talkdesk.conversation.auth.impl.ui.login.screens.WelcomeBackScreen
import org.talkdesk.conversation.auth.impl.ui.login.splashscreen.SplashTransition
import org.talkdesk.conversation.auth.impl.presentation.AccountError
import org.talkdesk.conversation.auth.impl.presentation.LoginEffect
import org.talkdesk.conversation.auth.impl.presentation.LoginIntent
import org.talkdesk.conversation.auth.impl.presentation.LoginPhase
import org.talkdesk.conversation.auth.impl.presentation.LoginViewModel
import org.talkdesk.conversation.core.designsystem.component.TalkdeskToastVariant
import org.talkdesk.conversation.core.designsystem.component.showTalkdeskToast
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons
import org.talkdesk.conversation.core.ui.CollectEffects
import kotlin.time.Duration.Companion.milliseconds
import androidx.core.net.toUri

/** How long to wait for a redirect after the user comes back from a Custom Tab before giving up. */
private const val CUSTOM_TAB_CANCEL_DELAY_MILLIS = 700L

/**
 * Login flow: splash, account name, welcome back and signing in, driven by [LoginViewModel].
 * Opens the sign-in page in an Auth Tab (Custom Tab fallback) and feeds the redirect back as an intent.
 *
 * @param incomingRedirects redirect URIs delivered to the activity by deep link. Only used by the
 *   Custom Tab fallback; the Auth Tab returns its result directly.
 */
@Composable
fun LoginFlow(
    incomingRedirects: Flow<String>,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = koinViewModel(),
) {

    val state by viewModel.state.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }
    var splashFinished by rememberSaveable { mutableStateOf(false) }
    var awaitingCustomTab by remember { mutableStateOf(false) }

    val authTabLauncher = rememberLauncherForActivityResult(AuthTabIntent.AuthenticateUserResultContract()) { result ->
        val uri = result.resultUri
        if (result.resultCode == AuthTabIntent.RESULT_OK && uri != null) {
            viewModel.onIntent(LoginIntent.RedirectReceived(uri.toString()))
        } else {
            viewModel.onIntent(LoginIntent.BrowserClosed)
        }
    }

    val offlineMessage = stringResource(R.string.auth_offline_message)
    val retryLabel = stringResource(R.string.auth_retry)
    val offlineIcon = TalkdeskIcons.WifiOff

    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            is LoginEffect.OpenBrowser -> {
                val uri = effect.url.toUri()
                val authTabPackage = authTabPackage(context)
                if (authTabPackage != null) {
                    AuthTabIntent.Builder().build().launch(authTabLauncher, uri, effect.redirectScheme)
                } else {
                    awaitingCustomTab = true
                    CustomTabsIntent.Builder().build().launchUrl(context, uri)
                }
            }
            // Launched separately: the snackbar stays until dismissed and must not block later effects.
            LoginEffect.ShowOffline -> scope.launch {
                val result = snackbarHostState.showTalkdeskToast(
                    message = offlineMessage,
                    variant = TalkdeskToastVariant.Error,
                    actionLabel = retryLabel,
                    icon = offlineIcon,
                    duration = SnackbarDuration.Indefinite,
                )
                if (result == SnackbarResult.ActionPerformed) viewModel.onIntent(LoginIntent.Continue)
            }
        }
    }

    CollectEffects(incomingRedirects) {
        awaitingCustomTab = false
        viewModel.onIntent(LoginIntent.RedirectReceived(it))
    }

    // Custom Tab fallback has no "closed" callback: if the user returns without a redirect, cancel.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (awaitingCustomTab) {
            scope.launch {
                delay(CUSTOM_TAB_CANCEL_DELAY_MILLIS.milliseconds)
                if (awaitingCustomTab) {
                    awaitingCustomTab = false
                    viewModel.onIntent(LoginIntent.BrowserClosed)
                }
            }
        }
    }

    SplashTransition(
        isReady = state.isReady,
        modifier = modifier,
        onFinished = { splashFinished = true },
    ) {
        when (state.phase) {
            LoginPhase.WelcomeBack -> WelcomeBackScreen(
                accountName = state.accountName,
                onContinue = { viewModel.onIntent(LoginIntent.Continue) },
                onUseDifferentAccount = { viewModel.onIntent(LoginIntent.UseDifferentAccount) },
                isOpeningSignIn = state.isOpeningSignIn,
            )

            LoginPhase.SigningIn -> SigningInScreen(accountName = state.accountName)

            LoginPhase.Restoring, LoginPhase.AccountEntry, LoginPhase.SignedIn -> AccountNameScreen(
                accountName = state.accountName,
                onAccountNameChange = { viewModel.onIntent(LoginIntent.AccountNameChanged(it)) },
                onContinue = { viewModel.onIntent(LoginIntent.Continue) },
                onHelpClick = {},
                errorText = state.accountError?.let { stringResource(it.messageRes()) },
                banner = state.banner,
                isOpeningSignIn = state.isOpeningSignIn,
                showHelpLink = false,
                requestFocus = splashFinished && state.phase == LoginPhase.AccountEntry,
                snackbarHostState = snackbarHostState,
            )
        }
    }
}

private fun AccountError.messageRes() = when (this) {
    AccountError.Empty -> R.string.auth_account_name_error_empty
    AccountError.Invalid -> R.string.auth_account_name_error_invalid
}

/** The default browser's package when it supports Auth Tab, otherwise null (use a Custom Tab). */
private fun authTabPackage(context: Context): String? =
    CustomTabsClient.getPackageName(context, null)
        ?.takeIf { CustomTabsClient.isAuthTabSupported(context, it) }
