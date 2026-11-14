import ConversationKit
import SwiftUI

@main
struct iOSApp: App {
    init() {
        // Composition root: starts Koin; Swift supplies what only Swift can implement.
        ConversationBootstrap.shared.start(
            es256Signer: CryptoKitEs256Signer(),
            credentials: BundleOAuthCredentials(),
            calling: CallingPlatformHost.shared.platform
        )
        CallingPlatformHost.shared.startPushRegistry()
    }

    var body: some Scene {
        WindowGroup {
            ConversationRoot()
                .environmentObject(CallingPlatformHost.shared)
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
                HomeView(onSignOut: session.signOut)
            } else {
                LoginFlowView()
            }
        }
        .task { await session.observe() }
    }
}
