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
/// placeholder; Account's has Sign out.
///
/// The Voicemails "new" dot of the design has no native equivalent (`badge` draws text or a count), so it is left out
/// until voicemail data exists.
struct HomeView: View {
    let onSignOut: () -> Void

    @State private var selection: HomeTab = .activity

    var body: some View {
        TabView(selection: $selection) {
            Tab(value: HomeTab.activity) {
                ActivityView()
            } label: {
                label(.activity)
            }
            Tab(value: HomeTab.contacts) {
                TabPlaceholderView(tab: .contacts, onSignOut: onSignOut)
            } label: {
                label(.contacts)
            }
            Tab(value: HomeTab.conversations) {
                TabPlaceholderView(tab: .conversations, onSignOut: onSignOut)
            } label: {
                label(.conversations)
            }
            Tab(value: HomeTab.voicemails) {
                TabPlaceholderView(tab: .voicemails, onSignOut: onSignOut)
            } label: {
                label(.voicemails)
            }
            Tab(value: HomeTab.account) {
                TabPlaceholderView(tab: .account, onSignOut: onSignOut)
            } label: {
                label(.account)
            }
        }
        .tint(TalkdeskColors.textBrand)
    }

    private func label(_ tab: HomeTab) -> some View {
        Label {
            Text(tab.title)
        } icon: {
            tab.icon
        }
    }
}

/// Stand-in for the tabs that are not designed yet. Account keeps a sign-out action so the session can still end.
private struct TabPlaceholderView: View {
    let tab: HomeTab
    let onSignOut: () -> Void

    var body: some View {
        NavigationStack {
            TalkdeskEmptyState(
                icon: tab.icon,
                title: "Coming soon",
                message: LocalizedStringKey("This tab isn't available in this version yet."),
                actionTitle: tab == .account ? "Sign out" : nil,
                action: tab == .account ? onSignOut : nil
            )
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(TalkdeskColors.bgWindow)
            .navigationTitle(Text(tab.title))
        }
    }
}
