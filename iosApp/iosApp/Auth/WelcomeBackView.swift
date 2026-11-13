import SwiftUI

/// Login for a returning user (iOS board i08): the last account that signed in, Continue, and
/// "Use a different account". Stateless; no keyboard on entry.
struct WelcomeBackView: View {
    let accountName: String
    var isOpeningSignIn: Bool = false
    let onContinue: () -> Void
    let onUseDifferentAccount: () -> Void

    var body: some View {
        LoginFormLayout(
            title: "Welcome back",
            description: "Continue to your company's sign-in page."
        ) {
            TalkdeskCard {
                RememberedAccountRow(accountName: accountName)
            }
            TalkdeskButton(
                isOpeningSignIn ? "Opening sign-in…" : "Continue",
                fullWidth: true,
                isLoading: isOpeningSignIn,
                action: onContinue
            )
            .padding(.top, TalkdeskSpacing.s24)
            TalkdeskButton("Use a different account", variant: .ghost, fullWidth: true, action: onUseDifferentAccount)
                .padding(.top, TalkdeskSpacing.s8)
        }
    }
}

#Preview("08 Welcome back · Light") {
    WelcomeBackView(accountName: "acme", onContinue: {}, onUseDifferentAccount: {}).talkdeskTheme()
}

#Preview("08 Welcome back · Dark") {
    WelcomeBackView(accountName: "acme", onContinue: {}, onUseDifferentAccount: {})
        .talkdeskTheme()
        .preferredColorScheme(.dark)
}
