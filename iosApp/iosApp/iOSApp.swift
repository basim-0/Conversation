import SwiftUI

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            ConversationRoot()
                .talkdeskTheme()
        }
    }
}

/// Temporary root until navigation and the login ViewModel exist: the splash hands off to the
/// account-name screen, which only keeps the typed name locally.
private struct ConversationRoot: View {
    @State private var accountName = ""

    var body: some View {
        // TODO: pass the session check's readiness once it exists.
        SplashTransition(isReady: true) {
            AccountNameView(accountName: $accountName, showsHelpLink: false, onContinue: {}, onHelp: {})
        }
    }
}
