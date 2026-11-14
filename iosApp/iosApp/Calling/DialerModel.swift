import AVFoundation
import ConversationKit
import SwiftUI

/// Owns the shared `DialerViewModel` for the lifetime of the New call sheet. iOS does not clear ViewModels
/// automatically, so `deinit` clears the scope. Same state, intents and effects as Android's `DialerScreen`.
@MainActor
final class DialerModel: ObservableObject {
    @Published private(set) var state: DialerState
    @Published var toast: TalkdeskToastItem?
    @Published var showsCountryPicker = false

    private let scope: ScopedViewModel<DialerViewModel>
    private let onCallStarted: () -> Void
    private let onClose: () -> Void

    init(onCallStarted: @escaping () -> Void, onClose: @escaping () -> Void) {
        self.onCallStarted = onCallStarted
        self.onClose = onClose
        scope = ConversationBootstrap.shared.makeDialerViewModel()
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
            case .callStarted:
                onCallStarted()
            case .callFailed(let failed):
                UINotificationFeedbackGenerator().notificationOccurred(.error)
                toast = TalkdeskToastItem(message: failureMessage(failed.failure), variant: .error)
            case .navigateBack:
                onClose()
            case .openCountryPicker:
                showsCountryPicker = true
            }
        }
    }

    func press(_ key: Character) {
        guard let code = key.asciiValue else { return }
        scope.viewModel.onIntent(intent: DialerIntentKeyPressed(key: UInt16(code)))
    }

    func deleteDigit() { scope.viewModel.onIntent(intent: DialerIntentDeleteDigit.shared) }
    func clearNumber() { scope.viewModel.onIntent(intent: DialerIntentClearNumber.shared) }
    func pasteNumber(_ text: String) { scope.viewModel.onIntent(intent: DialerIntentPasteNumber(text: text)) }
    func pickCountry() { scope.viewModel.onIntent(intent: DialerIntentPickCountry.shared) }
    func pickContact() { scope.viewModel.onIntent(intent: DialerIntentPickContact.shared) }
    func useSuggestedContact() { scope.viewModel.onIntent(intent: DialerIntentUseSuggestedContact.shared) }

    /// Calls once the microphone is allowed, asking for it first when needed.
    func call() async {
        let allowed = await Self.microphoneAllowed()
        guard allowed else {
            toast = TalkdeskToastItem(message: "Allow microphone access to make and answer calls", variant: .error)
            return
        }
        scope.viewModel.onIntent(intent: DialerIntentCall.shared)
    }

    private func failureMessage(_ failure: CallFailure) -> LocalizedStringKey {
        switch failure {
        case .busy: "Finish your current call first"
        case .offline: "No connection. Check your network and try again"
        case .other: "Couldn't connect the call"
        }
    }

    static func microphoneAllowed() async -> Bool {
        switch AVAudioApplication.shared.recordPermission {
        case .granted: return true
        case .denied: return false
        default: return await AVAudioApplication.requestRecordPermission()
        }
    }
}

/// The flag of a region as text (regional indicator letters), e.g. "US" -> flag. Data, not decoration: it is derived
/// from the ISO code the shared country list carries.
func flag(forRegion isoCode: String) -> String {
    isoCode.uppercased().unicodeScalars
        .compactMap { UnicodeScalar(127_397 + $0.value) }
        .map { String($0) }
        .joined()
}
