import ConversationKit
import SwiftUI

/// Owns the shared `ActivityViewModel` for the lifetime of the Activity tab. iOS does not clear ViewModels
/// automatically, so `deinit` clears the scope (cancelling the ViewModel's coroutines). Same state and intents
/// as Android's `ActivityScreen`.
@MainActor
final class ActivityModel: ObservableObject {
    @Published private(set) var state: ActivityState

    private let scope: ScopedViewModel<ActivityViewModel>

    init() {
        scope = ConversationBootstrap.shared.makeActivityViewModel()
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

    func selectTab(_ tab: ActivityTab) {
        scope.viewModel.onIntent(intent: ActivityIntentSelectTab(tab: tab))
    }

    func toggleChannel(_ channel: ActivityChannel) {
        scope.viewModel.onIntent(intent: ActivityIntentToggleChannel(channel: channel))
    }

    func clearFilters() {
        scope.viewModel.onIntent(intent: ActivityIntentClearFilters.shared)
    }

    func retry() {
        scope.viewModel.onIntent(intent: ActivityIntentRetry.shared)
    }

    func loadMore() {
        scope.viewModel.onIntent(intent: ActivityIntentLoadMore.shared)
    }

    func callBack(_ itemId: String) {
        scope.viewModel.onIntent(intent: ActivityIntentCallBack(itemId: itemId))
    }

    func open(_ itemId: String) {
        scope.viewModel.onIntent(intent: ActivityIntentOpenItem(itemId: itemId))
    }

    func newCall() {
        scope.viewModel.onIntent(intent: ActivityIntentNewCall.shared)
    }

    /// Pull to refresh: the spinner stays until the ViewModel reports the refresh finished.
    func refresh() async {
        scope.viewModel.onIntent(intent: ActivityIntentRefresh.shared)
        // `isRefreshing` is set synchronously by the intent, so reading it here is not racy.
        while scope.viewModel.state.value.isRefreshing {
            try? await Task.sleep(for: .milliseconds(100))
        }
    }
}
