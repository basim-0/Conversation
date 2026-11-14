import AVFoundation
import SwiftUI

/// Top-level destinations of the tab bar, in display order (same as Android's `HomeTab`).
enum HomeTab: CaseIterable, Identifiable {
    case activity, contacts, conversations, voicemails, account

    var id: Self { self }

    var title: LocalizedStringKey {
        switch self {
        case .activity: "Activity"
        case .contacts: "Contacts"
        case .conversations: "Conversations"
        case .voicemails: "Voicemails"
        case .account: "Account"
        }
    }

    var icon: Image {
        switch self {
        case .activity: TalkdeskIcons.clock
        case .contacts: TalkdeskIcons.contacts
        case .conversations: TalkdeskIcons.message
        case .voicemails: TalkdeskIcons.voicemail
        case .account: TalkdeskIcons.user
        }
    }
}

/// The signed-in shell: a native `TabView`, so the bar is the system tab bar (Liquid Glass on iOS 26+, with its
/// floating and minimising behaviour). Each tab keeps its own state. Tabs whose feature does not exist yet show a
/// placeholder.
///
/// The Voicemails "new" dot of the design has no native equivalent (`badge` draws text or a count), so it is left out
/// until voicemail data exists.
struct HomeView: View {
    let onSignOut: () -> Void

    @State private var selection: HomeTab = .activity
    @State private var showsDialer = false
    @EnvironmentObject private var calling: CallingPlatformHost
    @Environment(\.scenePhase) private var scenePhase

    var body: some View {
        TabView(selection: $selection) {
            Tab(value: HomeTab.activity) {
                ActivityView(onNewCall: { showsDialer = true })
            } label: {
                label(.activity)
            }
            Tab(value: HomeTab.contacts) {
                TabPlaceholderView(tab: .contacts)
            } label: {
                label(.contacts)
            }
            Tab(value: HomeTab.conversations) {
                TabPlaceholderView(tab: .conversations)
            } label: {
                label(.conversations)
            }
            Tab(value: HomeTab.voicemails) {
                TabPlaceholderView(tab: .voicemails)
            } label: {
                label(.voicemails)
            }
            Tab(value: HomeTab.account) {
                AccountView(onSignOut: onSignOut)
            } label: {
                label(.account)
            }
        }
        .tint(TalkdeskColors.textBrand)
        // "New call" is a page sheet; once the call starts the call screen takes over, like Android's CallActivity.
        .sheet(isPresented: $showsDialer) {
            DialerView(onCallStarted: { showsDialer = false }, onClose: { showsDialer = false })
        }
        .fullScreenCover(isPresented: Binding(
            get: { calling.showsCallScreen },
            set: { if !$0 { calling.isMinimized = true } }
        )) {
            CallView(onMinimize: { calling.isMinimized = true })
                .talkdeskTheme()
        }
        .onChange(of: scenePhase) { _, phase in
            // A minimised call comes back to the front with the app (the CallKit call screen's app button too).
            if phase == .active, calling.isInCall { calling.isMinimized = false }
        }
        // Answering a call from CallKit needs the microphone: ask once, before the first call.
        .task { _ = await DialerModel.microphoneAllowed() }
    }

    private func label(_ tab: HomeTab) -> some View {
        Label {
            Text(tab.title)
        } icon: {
            tab.icon
        }
    }
}

/// Stand-in for the tabs that are not designed yet.
private struct TabPlaceholderView: View {
    let tab: HomeTab

    var body: some View {
        NavigationStack {
            TalkdeskEmptyState(
                icon: tab.icon,
                title: "Coming soon",
                message: LocalizedStringKey("This tab isn't available in this version yet.")
            )
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(TalkdeskColors.bgWindow)
            .navigationTitle(Text(tab.title))
        }
    }
}
