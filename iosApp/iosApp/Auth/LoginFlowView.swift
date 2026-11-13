import ConversationKit
import SwiftUI

/// Owns the shared `LoginViewModel` for the lifetime of the login flow. iOS does not clear
/// ViewModels automatically, so `deinit` clears the scope (cancelling the ViewModel's coroutines).
@MainActor
final class LoginModel: ObservableObject {
    @Published private(set) var state: LoginState
    /// Set by the `ShowOffline` effect; cleared on the next user action.
    @Published private(set) var showsOffline = false

    private let scope: ScopedViewModel<LoginViewModel>
    private let authenticator = WebAuthenticator()

    init() {
        scope = ConversationBootstrap.shared.makeLoginViewModel()
        state = scope.viewModel.state.value
    }

    deinit {
        scope.clear()
    }

    func observeState() async {
        for await next in scope.viewModel.state {
            state = next
        }
    }

    func observeEffects() async {
        for await effect in scope.viewModel.effects {
            switch onEnum(of: effect) {
            case .openBrowser(let openBrowser):
                openSignIn(openBrowser)
            case .showOffline:
                showsOffline = true
            }
        }
    }

    func accountNameChanged(_ value: String) {
        showsOffline = false
        scope.viewModel.onIntent(intent: LoginIntentAccountNameChanged(value: value))
    }

    func continueSignIn() {
        showsOffline = false
        scope.viewModel.onIntent(intent: LoginIntentContinue.shared)
    }

    func useDifferentAccount() {
        showsOffline = false
        scope.viewModel.onIntent(intent: LoginIntentUseDifferentAccount.shared)
    }

    private func openSignIn(_ effect: LoginEffectOpenBrowser) {
        guard let url = URL(string: effect.url) else {
            scope.viewModel.onIntent(intent: LoginIntentBrowserClosed.shared)
            return
        }
        authenticator.start(url: url, callbackScheme: effect.redirectScheme) { [weak self] result in
            switch result {
            case .success(let redirect):
                self?.scope.viewModel.onIntent(intent: LoginIntentRedirectReceived(uri: redirect.absoluteString))
            case .failure:
                // Cancelled or closed without a redirect (same as Android's `BrowserClosed`).
                self?.scope.viewModel.onIntent(intent: LoginIntentBrowserClosed.shared)
            }
        }
    }
}

/// Login flow: splash, account name, welcome back and signing in, driven by the shared
/// `LoginViewModel` (same phases and mapping as Android's `LoginFlow`).
struct LoginFlowView: View {
    @StateObject private var model = LoginModel()

    var body: some View {
        SplashTransition(isReady: model.state.isReady) {
            switch model.state.phase {
            case .welcomeBack:
                WelcomeBackView(
                    accountName: model.state.accountName,
                    isOpeningSignIn: model.state.isOpeningSignIn,
                    onContinue: model.continueSignIn,
                    onUseDifferentAccount: model.useDifferentAccount
                )
            case .signingIn:
                SigningInView(accountName: model.state.accountName)
            default:
                AccountNameView(
                    accountName: Binding(
                        get: { model.state.accountName },
                        set: { model.accountNameChanged($0) }
                    ),
                    errorText: errorText(for: model.state.accountError),
                    banner: banner,
                    isOpeningSignIn: model.state.isOpeningSignIn,
                    showsHelpLink: false,
                    onContinue: model.continueSignIn,
                    onHelp: {}
                )
            }
        }
        .task { await model.observeState() }
        .task { await model.observeEffects() }
    }

    private var banner: LoginBannerStyle? {
        if model.showsOffline { return .offline }
        switch model.state.banner {
        case .signInDidNotFinish: return .signInDidNotFinish
        case .signInFailed: return .signInFailed
        case nil: return nil
        }
    }

    private func errorText(for error: AccountError?) -> LocalizedStringKey? {
        switch error {
        case .empty: "Enter your account name."
        case .invalid: "Use only letters, numbers and hyphens, without spaces."
        case nil: nil
        }
    }
}
