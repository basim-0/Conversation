package org.talkdesk.conversation.auth.impl.presentation

/** Which login screen to render. */
enum class LoginPhase {
    /** Checking the stored session (splash is still showing). */
    Restoring,

    /** Account name entry. */
    AccountEntry,

    /** Returning user with a remembered account (after logout or an expired session): continue as them. */
    WelcomeBack,

    /** Back from the browser, exchanging the code. */
    SigningIn,

    SignedIn,
}

enum class AccountError { Empty, Invalid }

enum class LoginBanner { SignInDidNotFinish, SignInFailed }

data class LoginState(
    val phase: LoginPhase = LoginPhase.Restoring,
    val accountName: String = "",
    val accountError: AccountError? = null,
    val banner: LoginBanner? = null,
    /** Looking up the account / opening the browser. */
    val isOpeningSignIn: Boolean = false,
) {
    /** True once the session check finished (drives the splash hand-off). */
    val isReady: Boolean get() = phase != LoginPhase.Restoring
}

sealed interface LoginIntent {
    data class AccountNameChanged(val value: String) : LoginIntent
    data object Continue : LoginIntent
    data object UseDifferentAccount : LoginIntent

    /** The browser returned to the app with [uri] (`com.talkdesk.agent://auth?...`). */
    data class RedirectReceived(val uri: String) : LoginIntent

    /** The browser was closed without returning to the app. */
    data object BrowserClosed : LoginIntent
}

sealed interface LoginEffect {
    /** Open [url] in the platform's browser session (Auth Tab / Custom Tab, ASWebAuthenticationSession). */
    data class OpenBrowser(val url: String, val redirectScheme: String) : LoginEffect

    /** No connection while looking up the account. The UI offers a retry (send [LoginIntent.Continue]). */
    data object ShowOffline : LoginEffect
}
