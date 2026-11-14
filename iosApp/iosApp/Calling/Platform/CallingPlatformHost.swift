import ConversationKit
import Foundation
import SwiftUI

/// Builds the Swift side of calling (Twilio, CallKit, PushKit, phone numbers) and tells the app when the call screen
/// should be shown. Created once, before the shared Kotlin graph starts.
@MainActor
final class CallingPlatformHost: ObservableObject {
    /// An outgoing call, or an incoming one the agent answered, is in progress: show the call screen.
    @Published private(set) var isInCall = false
    /// The agent dismissed the call screen while the call goes on. Shown again when the app returns to the front.
    @Published var isMinimized = false

    /// One host for the process: Koin needs the calling ports at start, before any view exists.
    static let shared = CallingPlatformHost()

    let platform: CallingPlatform

    private let voice = IosVoiceGateway()
    private let system = IosSystemCallGateway()
    private var pushRegistry: VoipPushRegistry?

    private init() {
        platform = CallingPlatform(
            voiceGateway: voice,
            systemCallGateway: system,
            phoneNumberParser: IosPhoneNumberParser(),
            clientInfo: ClientInfo(platform: "ios", version: Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String ?? "")
        )
        system.onAudioSessionChanged = { [voice] active in voice.setAudioEnabled(active) }
        system.onDTMF = { [voice] digits in voice.sendDigitsToActiveCall(digits) }
        voice.onInviteDropped = { [system] in system.endProvisional(reason: .remoteEnded) }
        system.onConnectedCallsChanged = { [weak self] calls in
            guard let self else { return }
            let inCall = !calls.isEmpty
            if inCall && !self.isInCall { self.isMinimized = false }
            self.isInCall = inCall
        }
    }

    /// Starts listening for VoIP pushes. Call once the Kotlin graph is running, as a push can arrive immediately.
    func startPushRegistry() {
        pushRegistry = VoipPushRegistry(voice: voice, system: system)
    }

    /// The call screen is wanted: a call is on and the agent has not minimised it.
    var showsCallScreen: Bool { isInCall && !isMinimized }
}
