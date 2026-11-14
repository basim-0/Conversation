import CallKit
import ConversationKit
import Foundation
import PushKit

/// Receives the VoIP push (PushKit) that announces an incoming call, and the push token the voice network needs.
/// Created once at launch: iOS delivers a VoIP push by launching the app in the background, and the push must be
/// reported to CallKit before the handler returns.
final class VoipPushRegistry: NSObject, PKPushRegistryDelegate {
    private let registry = PKPushRegistry(queue: .main)
    private let voice: IosVoiceGateway
    private let system: IosSystemCallGateway

    init(voice: IosVoiceGateway, system: IosSystemCallGateway) {
        self.voice = voice
        self.system = system
        super.init()
        // The delegate must be set before the desired push types.
        registry.delegate = self
        registry.desiredPushTypes = [.voIP]
    }

    func pushRegistry(_ registry: PKPushRegistry, didUpdate pushCredentials: PKPushCredentials, for type: PKPushType) {
        guard type == .voIP else { return }
        let token = pushCredentials.token.map { String(format: "%02x", $0) }.joined()
        ConversationBootstrap.shared.pushTokens.update(token: token)
    }

    func pushRegistry(_ registry: PKPushRegistry, didInvalidatePushTokenFor type: PKPushType) {}

    func pushRegistry(
        _ registry: PKPushRegistry,
        didReceiveIncomingPushWith payload: PKPushPayload,
        for type: PKPushType,
        completion: @escaping () -> Void
    ) {
        guard type == .voIP else {
            completion()
            return
        }
        let isCancel = (payload.dictionaryPayload["twi_message_type"] as? String) == "twilio.voice.cancel"
        // Every VoIP push must be reported to CallKit. A cancellation has no call to show: report and drop at once.
        system.reportProvisionalIncoming {
            completion()
        }
        let handled = voice.handleVoipPush(payload.dictionaryPayload)
        if isCancel || !handled {
            system.endProvisional(reason: isCancel ? .remoteEnded : .failed)
        }
    }
}
