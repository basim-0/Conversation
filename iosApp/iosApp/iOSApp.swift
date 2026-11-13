import ConversationKit
import SwiftUI

@main
struct iOSApp: App {
    init() {
        // Composition root: starts Koin; Swift supplies what only Swift can implement.
        ConversationBootstrap.shared.start(
            es256Signer: CryptoKitEs256Signer(),
            credentials: BundleOAuthCredentials()
        )
    }

    var body: some Scene {
        WindowGroup {
            ConversationRoot()
                .talkdeskTheme()
        }
    }
}

/// Follows the auth session like Android's `ConversationRoot`: signed in shows the app,
/// anything else (including the initial check) shows the login flow with the splash.
@MainActor
final class SessionModel: ObservableObject {
    @Published private(set) var isSignedIn = false

    func observe() async {
        for await state in ConversationBootstrap.shared.authSession.state {
            if case .signedIn = onEnum(of: state) {
                isSignedIn = true
            } else {
                isSignedIn = false
            }
        }
    }

    func signOut() {
        // Logout clears the local session even if the server call fails, so errors are not surfaced.
        Task { try? await ConversationBootstrap.shared.authSession.logout() }
    }
}

private struct ConversationRoot: View {
    @StateObject private var session = SessionModel()

    var body: some View {
        Group {
            if session.isSignedIn {
                SignedInPlaceholder(onSignOut: session.signOut)
            } else {
                LoginFlowView()
            }
        }
        .task { await session.observe() }
    }
}

// TODO: temporary signed-in screen so sign-out can be exercised. Replace with the real home once designed.
private struct SignedInPlaceholder: View {
    let onSignOut: () -> Void

    var body: some View {
        ZStack {
            TalkdeskColors.bgWindow.ignoresSafeArea()
            TalkdeskButton("Sign out", action: onSignOut)
        }
    }
}
