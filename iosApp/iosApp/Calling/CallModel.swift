import ConversationKit
import SwiftUI

/// Owns the shared `CallViewModel` for as long as the call screen is on. iOS does not clear ViewModels automatically,
/// so `deinit` clears the scope. Same state, intents and effects as Android's `CallScreen`.
@MainActor
final class CallModel: ObservableObject {
    @Published private(set) var state: CallState

    private let scope: ScopedViewModel<CallViewModel>
    private let onMinimize: () -> Void

    init(onMinimize: @escaping () -> Void) {
        self.onMinimize = onMinimize
        scope = ConversationBootstrap.shared.makeCallViewModel()
        state = scope.viewModel.state.value
    }

    deinit {
        scope.clear()
    }

    func observeState() async {
        for await next in scope.viewModel.state { state = next }
    }

    func observeEffects() async {
        for await effect in scope.viewModel.effects {
            switch onEnum(of: effect) {
            case .minimized:
                onMinimize()
            case .callEnded:
                // The call screen closes when CallKit reports the end (`CallingPlatformHost.isInCall`).
                break
            }
        }
    }

    func toggleMute() { scope.viewModel.onIntent(intent: CallIntentToggleMute.shared) }
    func toggleHold() { scope.viewModel.onIntent(intent: CallIntentToggleHold.shared) }
    func toggleSpeaker() { scope.viewModel.onIntent(intent: CallIntentToggleSpeaker.shared) }
    func showKeypad() { scope.viewModel.onIntent(intent: CallIntentShowKeypad.shared) }
    func hideKeypad() { scope.viewModel.onIntent(intent: CallIntentHideKeypad.shared) }
    func endCall() { scope.viewModel.onIntent(intent: CallIntentEndCall.shared) }
    func minimize() { scope.viewModel.onIntent(intent: CallIntentMinimize.shared) }

    func sendDigit(_ digit: Character) {
        guard let code = digit.asciiValue else { return }
        scope.viewModel.onIntent(intent: CallIntentSendDigit(digit: UInt16(code)))
    }
}

/// Call length as `mm:ss`, or `h:mm:ss` from one hour. Digits only, so it needs no translation.
func formatElapsed(_ totalSeconds: Int) -> String {
    let hours = totalSeconds / 3600
    let minutes = totalSeconds % 3600 / 60
    let seconds = totalSeconds % 60
    return hours > 0
        ? String(format: "%d:%02d:%02d", hours, minutes, seconds)
        : String(format: "%02d:%02d", minutes, seconds)
}
