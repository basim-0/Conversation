import ConversationKit
import SwiftUI

/// Owns the shared `AccountViewModel` for the lifetime of the Account tab. iOS does not clear ViewModels
/// automatically, so `deinit` clears the scope (cancelling the ViewModel's coroutines). Same state, intents and
/// effects as Android's `AccountScreen`.
@MainActor
final class AccountModel: ObservableObject {
    @Published private(set) var state: AccountState
    /// The last status change failed: shown as an inline banner with Retry until the next successful change.
    @Published private(set) var statusChangeFailed = false
    /// Bumped on each confirmed change, to drive the selection haptic.
    @Published private(set) var confirmedChanges = 0
    @Published private(set) var failures = 0

    private let scope: ScopedViewModel<AccountViewModel>
    private let onSignOut: () -> Void

    init(onSignOut: @escaping () -> Void) {
        self.onSignOut = onSignOut
        scope = ConversationBootstrap.shared.makeAccountViewModel()
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
            case .statusChanged(let changed):
                statusChangeFailed = false
                confirmedChanges += 1
                // iOS has no snackbar: VoiceOver announces what Android shows in its toast.
                UIAccessibility.post(
                    notification: .announcement,
                    argument: String(localized: "Status set to \(changed.status.displayName)")
                )
            case .statusChangeFailed:
                statusChangeFailed = true
                failures += 1
            case .signOut:
                onSignOut()
            }
        }
    }

    func select(_ statusId: String) { scope.viewModel.onIntent(intent: AccountIntentSelectStatus(statusId: statusId)) }
    func retry() { scope.viewModel.onIntent(intent: AccountIntentRetryStatusChange.shared) }
    func sendFeedback() { scope.viewModel.onIntent(intent: AccountIntentSendFeedback.shared) }
    func logOutClicked() { scope.viewModel.onIntent(intent: AccountIntentLogOutClicked.shared) }
    func confirmLogOut() { scope.viewModel.onIntent(intent: AccountIntentConfirmLogOut.shared) }
    func dismissDialog() { scope.viewModel.onIntent(intent: AccountIntentDismissDialog.shared) }
}

extension AgentStatus {
    /// The account's own name for custom statuses, a localised name for the rest.
    var displayName: String {
        if let label { return label }
        return switch kind {
        case .available: String(localized: "Available")
        case .away: String(localized: "Away")
        case .offline: String(localized: "Offline")
        case .busy, .unknown: String(localized: "Busy")
        case .afterCallWork: String(localized: "After call work")
        }
    }
}

extension PresenceKind {
    var spokenName: String {
        switch self {
        case .available: String(localized: "Available")
        case .away: String(localized: "Away")
        case .offline: String(localized: "Offline")
        case .busy: String(localized: "Busy")
        }
    }
}

extension StatusGroupKind {
    var title: LocalizedStringKey {
        switch self {
        case .available: "Available"
        case .away: "Away"
        case .offline: "Offline"
        case .automatic: "Set automatically"
        }
    }
}
