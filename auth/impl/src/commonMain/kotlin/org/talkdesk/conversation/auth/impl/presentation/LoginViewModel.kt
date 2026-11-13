package org.talkdesk.conversation.auth.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.talkdesk.conversation.auth.api.AuthSession
import org.talkdesk.conversation.auth.api.SessionState
import org.talkdesk.conversation.auth.impl.AuthConfig
import org.talkdesk.conversation.auth.impl.domain.utils.AuthError
import org.talkdesk.conversation.auth.impl.domain.usecase.CancelSignInUseCase
import org.talkdesk.conversation.auth.impl.domain.usecase.CompleteSignInUseCase
import org.talkdesk.conversation.auth.impl.domain.usecase.ForgetAccountUseCase
import org.talkdesk.conversation.auth.impl.domain.usecase.RestoreSessionUseCase
import org.talkdesk.conversation.auth.impl.domain.usecase.StartSignInUseCase

class LoginViewModel internal constructor(
    private val session: AuthSession,
    private val restoreSession: RestoreSessionUseCase,
    private val startSignIn: StartSignInUseCase,
    private val cancelSignIn: CancelSignInUseCase,
    private val completeSignIn: CompleteSignInUseCase,
    private val forgetAccount: ForgetAccountUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private val _effects = Channel<LoginEffect>(Channel.BUFFERED)
    val effects: Flow<LoginEffect> = _effects.receiveAsFlow()

    init {
        viewModelScope.launch { session.state.collect(::onSessionChanged) }
        viewModelScope.launch { restoreSession() }
    }

    fun onIntent(intent: LoginIntent) {
        viewModelScope.launch { handle(intent) }
    }

    private suspend fun handle(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.AccountNameChanged ->
                setState { copy(accountName = intent.value, accountError = null, banner = null) }
            LoginIntent.Continue -> continueToBrowser()
            LoginIntent.UseDifferentAccount -> {
                cancelSignIn()
                forgetAccount()
                setState { LoginState(phase = LoginPhase.AccountEntry) }
            }
            is LoginIntent.RedirectReceived -> finishSignIn(intent.uri)
            LoginIntent.BrowserClosed -> {
                cancelSignIn()
                setState { copy(isOpeningSignIn = false, banner = LoginBanner.SignInDidNotFinish) }
            }
        }
    }

    private fun onSessionChanged(sessionState: SessionState) {
        when (sessionState) {
            SessionState.Loading -> Unit
            is SessionState.SignedIn -> setState { copy(phase = LoginPhase.SignedIn, isOpeningSignIn = false) }
            is SessionState.SignedOut -> setState {
                // While the browser/code exchange is running the screen is driven by the intents.
                if (phase == LoginPhase.SigningIn || isOpeningSignIn) {
                    this
                } else {
                    val remembered = sessionState.accountName
                    copy(
                        phase = if (remembered != null) {
                            LoginPhase.WelcomeBack
                        } else {
                            LoginPhase.AccountEntry
                        },
                        accountName = remembered ?: accountName,
                    )
                }
            }
        }
    }

    private suspend fun continueToBrowser() {
        if (state.value.isOpeningSignIn) return
        setState { copy(isOpeningSignIn = true, accountError = null, banner = null) }
        startSignIn(state.value.accountName).fold(
            onSuccess = { send(LoginEffect.OpenBrowser(it.url, AuthConfig.REDIRECT_SCHEME)) },
            onFailure = { error ->
                setState { copy(isOpeningSignIn = false) }
                when (error) {
                    AuthError.AccountEmpty -> setState { copy(accountError = AccountError.Empty) }
                    AuthError.AccountInvalid -> setState { copy(accountError = AccountError.Invalid) }
                    AuthError.AccountNotFound -> setState { copy(banner = LoginBanner.SignInDidNotFinish) }
                    is AuthError.Offline -> send(LoginEffect.ShowOffline)
                    else -> setState { copy(banner = LoginBanner.SignInFailed) }
                }
            },
        )
    }

    private suspend fun finishSignIn(uri: String) {
        setState { copy(phase = LoginPhase.SigningIn, isOpeningSignIn = false, banner = null) }
        completeSignIn(uri).fold(
            onSuccess = { setState { copy(phase = LoginPhase.SignedIn) } },
            onFailure = { error ->
                // TODO: dedicated "not whitelisted" screen once it exists in the design system.
                if (error is AuthError.Offline) send(LoginEffect.ShowOffline)
                setState {
                    copy(
                        phase = LoginPhase.AccountEntry,
                        banner = if (error is AuthError.Offline) null else LoginBanner.SignInFailed,
                    )
                }
            },
        )
    }

    private fun setState(reduce: LoginState.() -> LoginState) = _state.update(reduce)

    private fun send(effect: LoginEffect) {
        _effects.trySend(effect)
    }
}
