import ConversationKit
import Foundation
import TwilioVoice

/// The voice network on iOS: Twilio Voice. Port of Android's `TwilioVoiceGateway`, with the legacy iOS app's call
/// parameters. It knows nothing about CallKit or MVI; `IosCallPlatform` connects the two.
///
/// Call audio is owned by CallKit: the audio device stays disabled until CallKit activates the audio session
/// (`setAudioEnabled`), as Twilio requires.
final class IosVoiceGateway: NSObject, VoiceGateway {
    private let lock = NSLock()
    private var listener: VoiceListener?
    private var invites: [String: CallInvite] = [:]
    private var calls: [String: TwilioVoice.Call] = [:]
    private var qualityTimers: [String: Timer] = [:]
    private let audioDevice = DefaultAudioDevice()

    /// An invite ended without a call being created for it (rejected, cancelled): CallKit must drop its provisional call.
    var onInviteDropped: (() -> Void)?

    override init() {
        super.init()
        TwilioVoiceSDK.audioDevice = audioDevice
        audioDevice.isEnabled = false
    }

    /// CallKit activated (`true`) or deactivated the audio session.
    func setAudioEnabled(_ enabled: Bool) {
        audioDevice.isEnabled = enabled
    }

    /// A VoIP push arrived. Twilio parses it and reports a call invite (or a cancellation) to this gateway.
    func handleVoipPush(_ payload: [AnyHashable: Any]) -> Bool {
        TwilioVoiceSDK.handleNotification(payload, delegate: self, delegateQueue: nil)
    }

    /// Tones from the system call screen.
    func sendDigitsToActiveCall(_ digits: String) {
        lock.withLock { calls.values.first(where: { $0.state == .connected }) }?.sendDigits(digits)
    }

    // MARK: VoiceGateway

    func setListener(listener: VoiceListener?) {
        lock.withLock { self.listener = listener }
    }

    func register(accessToken: String, pushToken: String, onResult: @escaping (String?) -> Void) {
        guard let data = Self.data(fromHex: pushToken) else {
            onResult("Invalid push token")
            return
        }
        TwilioVoiceSDK.register(accessToken: accessToken, deviceToken: data) { error in
            onResult(error?.localizedDescription)
        }
    }

    func unregister(accessToken: String, pushToken: String, onResult: @escaping (String?) -> Void) {
        guard let data = Self.data(fromHex: pushToken) else {
            onResult("Invalid push token")
            return
        }
        TwilioVoiceSDK.unregister(accessToken: accessToken, deviceToken: data) { error in
            onResult(error?.localizedDescription)
        }
    }

    func connect(accessToken: String, parameters: [String: String]) -> String {
        let uuid = UUID()
        // Same parameters as Android: `to`, `ToNumber` and `AgentId` come from the shared CallManager.
        let params = parameters
        let options = ConnectOptions(accessToken: accessToken) { builder in
            builder.params = params
            builder.uuid = uuid
        }
        let call = TwilioVoiceSDK.connect(options: options, delegate: self)
        lock.withLock { calls[uuid.uuidString] = call }
        return uuid.uuidString
    }

    func accept(handle: String) {
        guard let invite = lock.withLock({ invites.removeValue(forKey: handle) }) else { return }
        let options = AcceptOptions(callInvite: invite) { builder in
            builder.uuid = invite.uuid
        }
        let call = invite.accept(options: options, delegate: self)
        lock.withLock { calls[handle] = call }
    }

    func reject(handle: String) {
        guard let invite = lock.withLock({ invites.removeValue(forKey: handle) }) else { return }
        invite.reject()
        onInviteDropped?()
    }

    func disconnect(handle: String) {
        lock.withLock { calls[handle] }?.disconnect()
    }

    func setMuted(handle: String, muted: Bool) {
        lock.withLock { calls[handle] }?.isMuted = muted
    }

    func sendDigits(handle: String, digits: String) {
        lock.withLock { calls[handle] }?.sendDigits(digits)
    }

    // MARK: Helpers

    private func handle(of call: TwilioVoice.Call) -> String? {
        call.uuid?.uuidString
    }

    private var currentListener: VoiceListener? { lock.withLock { listener } }

    private func startQualityPolling(_ call: TwilioVoice.Call, handle: String) {
        DispatchQueue.main.async { [weak self] in
            self?.qualityTimers[handle]?.invalidate()
            self?.qualityTimers[handle] = Timer.scheduledTimer(withTimeInterval: 5, repeats: true) { [weak self] _ in
                guard call.state == .connected else { return }
                call.getStats { reports in
                    guard let mos = reports.first?.remoteAudioTrackStats.first?.mos else { return }
                    let quality: CallQuality = mos >= 4.2 ? .excellent : (mos >= 3.6 ? .good : .poor)
                    self?.currentListener?.onQualityChanged(handle: handle, quality: quality)
                }
            }
        }
    }

    private func stopQualityPolling(_ handle: String) {
        DispatchQueue.main.async { [weak self] in
            self?.qualityTimers.removeValue(forKey: handle)?.invalidate()
        }
    }

    private static func data(fromHex hex: String) -> Data? {
        guard hex.count % 2 == 0 else { return nil }
        var data = Data(capacity: hex.count / 2)
        var index = hex.startIndex
        while index < hex.endIndex {
            let next = hex.index(index, offsetBy: 2)
            guard let byte = UInt8(hex[index..<next], radix: 16) else { return nil }
            data.append(byte)
            index = next
        }
        return data
    }
}

extension IosVoiceGateway: NotificationDelegate {
    func callInviteReceived(callInvite: CallInvite) {
        let handle = callInvite.uuid.uuidString
        lock.withLock { invites[handle] = callInvite }
        currentListener?.onInvite(invite: VoiceInvite(
            handle: handle,
            callSid: callInvite.callSid,
            from: callInvite.from,
            parameters: callInvite.customParameters ?? [:]
        ))
    }

    func cancelledCallInviteReceived(cancelledCallInvite: CancelledCallInvite, error: Error) {
        // The invite is matched by call sid: a cancellation carries no uuid.
        let match = lock.withLock {
            invites.first(where: { $0.value.callSid == cancelledCallInvite.callSid })?.key
        }
        guard let handle = match else {
            onInviteDropped?()
            return
        }
        lock.withLock { _ = invites.removeValue(forKey: handle) }
        currentListener?.onInviteCancelled(handle: handle)
    }
}

extension IosVoiceGateway: CallDelegate {
    func callDidStartRinging(call: TwilioVoice.Call) {
        guard let handle = handle(of: call) else { return }
        currentListener?.onRinging(handle: handle, callSid: call.sid)
    }

    func callDidConnect(call: TwilioVoice.Call) {
        guard let handle = handle(of: call) else { return }
        lock.withLock { calls[handle] = call }
        currentListener?.onConnected(handle: handle, callSid: call.sid)
        startQualityPolling(call, handle: handle)
    }

    func callDidFailToConnect(call: TwilioVoice.Call, error: Error) {
        guard let handle = handle(of: call) else { return }
        lock.withLock { _ = calls.removeValue(forKey: handle) }
        stopQualityPolling(handle)
        currentListener?.onConnectFailure(handle: handle, error: error.localizedDescription)
    }

    func callDidDisconnect(call: TwilioVoice.Call, error: Error?) {
        guard let handle = handle(of: call) else { return }
        lock.withLock { _ = calls.removeValue(forKey: handle) }
        stopQualityPolling(handle)
        currentListener?.onDisconnected(handle: handle, error: error?.localizedDescription)
    }

    func call(call: TwilioVoice.Call, isReconnectingWithError error: Error) {
        guard let handle = handle(of: call) else { return }
        currentListener?.onReconnecting(handle: handle)
    }

    func callDidReconnect(call: TwilioVoice.Call) {
        guard let handle = handle(of: call) else { return }
        currentListener?.onReconnected(handle: handle)
    }
}
