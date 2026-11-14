import AVFoundation
import CallKit
import ConversationKit
import Foundation
import UIKit

/// The operating system's call framework on iOS: CallKit. Port of Android's `TelecomCallGateway`. The app tells
/// CallKit what happened; CallKit tells the app what the user or the system asked for (the call screen, a headset,
/// a watch, CarPlay, another call). Calls are identified by the domain call id, which is a UUID string and is used
/// as the CallKit call UUID.
///
/// Incoming calls: the VoIP push must be reported to CallKit before its handler returns (iOS 13+), but the domain
/// call only exists once Twilio and the shared `CallManager` have processed the invite. So the push handler
/// reports a *provisional* call (`reportProvisionalIncoming`) and `addCall` adopts it.
final class IosSystemCallGateway: NSObject, SystemCallGateway {
    private let provider: CXProvider
    private let controller = CXCallController()
    private let lock = NSLock()
    private var listener: SystemCallListener?
    private var provisional: UUID?
    private var provisionalTimeout: DispatchWorkItem?
    /// Domain call id -> CallKit uuid (differs only for an adopted provisional call).
    private var uuids: [String: UUID] = [:]
    private var outgoingConnected: Set<UUID> = []
    private var routeObserver: NSObjectProtocol?

    /// Calls the agent is in (answered incoming, or outgoing): the app shows its call screen for these. Main queue.
    var onConnectedCallsChanged: ((Set<String>) -> Void)?
    var onAudioSessionChanged: ((Bool) -> Void)?
    var onDTMF: ((String) -> Void)?
    private var connectedCalls: Set<String> = []

    override init() {
        let configuration = CXProviderConfiguration()
        configuration.maximumCallGroups = 1
        configuration.maximumCallsPerCallGroup = 1
        configuration.supportedHandleTypes = [.phoneNumber, .generic]
        // Keeps the call register out of the system's Recents.
        configuration.includesCallsInRecents = false
        configuration.supportsVideo = false
        configuration.iconTemplateImageData = UIImage(named: "LaunchMark")?.pngData()
        provider = CXProvider(configuration: configuration)
        super.init()
        provider.setDelegate(self, queue: nil)
        routeObserver = NotificationCenter.default.addObserver(
            forName: AVAudioSession.routeChangeNotification, object: nil, queue: nil
        ) { [weak self] _ in self?.publishRoutes() }
    }

    deinit {
        provider.invalidate()
        if let routeObserver { NotificationCenter.default.removeObserver(routeObserver) }
    }

    // MARK: Provisional incoming call (VoIP push)

    /// Reports an incoming call for a VoIP push straight away, so iOS does not terminate the app. Replaced by the real
    /// call in `addCall`, or ended after a short wait when none follows.
    func reportProvisionalIncoming(completion: @escaping () -> Void) {
        let uuid = UUID()
        lock.withLock { provisional = uuid }
        let update = CXCallUpdate()
        update.remoteHandle = CXHandle(type: .generic, value: "Talkdesk")
        update.localizedCallerName = String(localized: "Incoming call")
        update.supportsHolding = true
        update.supportsDTMF = true
        update.supportsGrouping = false
        update.supportsUngrouping = false
        update.hasVideo = false
        provider.reportNewIncomingCall(with: uuid, update: update) { _ in completion() }
        let timeout = DispatchWorkItem { [weak self] in self?.endProvisional(reason: .unanswered) }
        lock.withLock { provisionalTimeout = timeout }
        DispatchQueue.global().asyncAfter(deadline: .now() + 6, execute: timeout)
    }

    /// Drops the provisional call when no domain call took it over (invite rejected, cancelled or never processed).
    func endProvisional(reason: CXCallEndedReason) {
        let uuid: UUID? = lock.withLock {
            provisionalTimeout?.cancel()
            provisionalTimeout = nil
            defer { provisional = nil }
            return provisional
        }
        if let uuid { provider.reportCall(with: uuid, endedAt: Date(), reason: reason) }
    }

    // MARK: SystemCallGateway

    func setListener(listener: SystemCallListener?) {
        lock.withLock { self.listener = listener }
        publishRoutes()
    }

    func addCall(callId: String, number: String, displayName: String?, direction: CallDirection_, onResult: @escaping (String?) -> Void) {
        guard let domainUUID = UUID(uuidString: callId) else {
            onResult("Invalid call id")
            return
        }
        let handle = CXHandle(type: .phoneNumber, value: number)
        if direction == .incoming {
            let adopted: UUID? = lock.withLock {
                provisionalTimeout?.cancel()
                provisionalTimeout = nil
                defer { provisional = nil }
                return provisional
            }
            let uuid = adopted ?? domainUUID
            lock.withLock { uuids[callId] = uuid }
            let update = CXCallUpdate()
            update.remoteHandle = handle
            update.localizedCallerName = displayName ?? number
            update.supportsHolding = true
            update.supportsDTMF = true
            update.supportsGrouping = false
            update.supportsUngrouping = false
            update.hasVideo = false
            if adopted != nil {
                provider.reportCall(with: uuid, updated: update)
                onResult(nil)
            } else {
                provider.reportNewIncomingCall(with: uuid, update: update) { error in
                    onResult(error?.localizedDescription)
                }
            }
        } else {
            lock.withLock { uuids[callId] = domainUUID }
            let start = CXStartCallAction(call: domainUUID, handle: handle)
            start.contactIdentifier = displayName
            controller.request(CXTransaction(action: start)) { [weak self] error in
                if error == nil { self?.markConnected(callId) }
                onResult(error?.localizedDescription)
            }
        }
    }

    func answer(callId: String) {
        guard let uuid = uuid(for: callId) else { return }
        controller.request(CXTransaction(action: CXAnswerCallAction(call: uuid))) { _ in }
    }

    func setActive(callId: String) {
        guard let uuid = uuid(for: callId) else { return }
        // First time for an outgoing call: it connected. Later: it was resumed from hold.
        let firstConnect: Bool = lock.withLock { outgoingConnected.insert(uuid).inserted }
        if firstConnect, callId == uuid.uuidString {
            provider.reportOutgoingCall(with: uuid, connectedAt: Date())
        } else {
            controller.request(CXTransaction(action: CXSetHeldCallAction(call: uuid, onHold: false))) { _ in }
        }
    }

    func setInactive(callId: String) {
        guard let uuid = uuid(for: callId) else { return }
        controller.request(CXTransaction(action: CXSetHeldCallAction(call: uuid, onHold: true))) { _ in }
    }

    func disconnect(callId: String, reason: EndReason) {
        guard let uuid = lock.withLock({ uuids.removeValue(forKey: callId) }) else { return }
        lock.withLock { _ = outgoingConnected.remove(uuid) }
        markEnded(callId)
        switch reason {
        case .local, .rejected:
            // An agent's own end goes through CallKit so the system call screen closes with the right reason.
            controller.request(CXTransaction(action: CXEndCallAction(call: uuid))) { [weak self] error in
                if error != nil { self?.provider.reportCall(with: uuid, endedAt: Date(), reason: .remoteEnded) }
            }
        case .remote:
            provider.reportCall(with: uuid, endedAt: Date(), reason: .remoteEnded)
        case .missed:
            provider.reportCall(with: uuid, endedAt: Date(), reason: .unanswered)
        case .failed:
            provider.reportCall(with: uuid, endedAt: Date(), reason: .failed)
        default:
            provider.reportCall(with: uuid, endedAt: Date(), reason: .remoteEnded)
        }
    }

    func requestEndpoint(endpointId: String) {
        let session = AVAudioSession.sharedInstance()
        do {
            switch endpointId {
            case Endpoint.speaker: try session.overrideOutputAudioPort(.speaker)
            case Endpoint.earpiece: try session.overrideOutputAudioPort(.none)
            default:
                try session.overrideOutputAudioPort(.none)
                if let port = session.availableInputs?.first(where: { $0.uid == endpointId }) {
                    try session.setPreferredInput(port)
                }
            }
        } catch {
            // The route stays as it is; the UI reflects the real route through `publishRoutes`.
        }
        publishRoutes()
    }

    // MARK: Helpers

    private func uuid(for callId: String) -> UUID? { lock.withLock { uuids[callId] } }

    private func callId(for uuid: UUID) -> String? {
        lock.withLock { uuids.first(where: { $0.value == uuid })?.key }
    }

    private var currentListener: SystemCallListener? { lock.withLock { listener } }

    private func markConnected(_ callId: String) {
        DispatchQueue.main.async {
            self.connectedCalls.insert(callId)
            self.onConnectedCallsChanged?(self.connectedCalls)
        }
    }

    private func markEnded(_ callId: String) {
        DispatchQueue.main.async {
            self.connectedCalls.remove(callId)
            self.onConnectedCallsChanged?(self.connectedCalls)
        }
    }

    private enum Endpoint {
        static let earpiece = "earpiece"
        static let speaker = "speaker"
    }

    /// Where audio can go now and where it goes.
    func publishRoutes() {
        let session = AVAudioSession.sharedInstance()
        let outputs = session.currentRoute.outputs
        var available: [AudioEndpoint] = [
            AudioEndpoint(id: Endpoint.earpiece, type: .earpiece, name: String(localized: "iPhone")),
            AudioEndpoint(id: Endpoint.speaker, type: .speaker, name: String(localized: "Speaker")),
        ]
        var current: AudioEndpoint?
        for port in outputs {
            switch port.portType {
            case .builtInSpeaker:
                current = available.first { $0.id == Endpoint.speaker }
            case .builtInReceiver:
                current = available.first { $0.id == Endpoint.earpiece }
            case .bluetoothA2DP, .bluetoothHFP, .bluetoothLE:
                let endpoint = AudioEndpoint(id: port.uid, type: .bluetooth, name: port.portName)
                available.append(endpoint)
                current = endpoint
            case .headphones, .headsetMic, .usbAudio:
                let endpoint = AudioEndpoint(id: port.uid, type: .wired, name: port.portName)
                available.append(endpoint)
                current = endpoint
            default:
                break
            }
        }
        currentListener?.onEndpointsChanged(available: available, current: current)
    }
}

extension IosSystemCallGateway: CXProviderDelegate {
    func providerDidReset(_ provider: CXProvider) {
        // CallKit dropped every call (e.g. after a crash of the call service): end them in the domain too.
        let ids = lock.withLock { Array(uuids.keys) }
        ids.forEach { currentListener?.onDisconnectRequested(callId: $0) }
        lock.withLock {
            uuids.removeAll()
            outgoingConnected.removeAll()
        }
        DispatchQueue.main.async {
            self.connectedCalls.removeAll()
            self.onConnectedCallsChanged?([])
        }
        onAudioSessionChanged?(false)
    }

    func provider(_ provider: CXProvider, perform action: CXStartCallAction) {
        provider.reportOutgoingCall(with: action.callUUID, startedConnectingAt: Date())
        action.fulfill()
    }

    func provider(_ provider: CXProvider, perform action: CXAnswerCallAction) {
        if let id = callId(for: action.callUUID) {
            markConnected(id)
            currentListener?.onAnswerRequested(callId: id)
        }
        action.fulfill()
    }

    func provider(_ provider: CXProvider, perform action: CXEndCallAction) {
        if let id = callId(for: action.callUUID) {
            currentListener?.onDisconnectRequested(callId: id)
        } else if lock.withLock({ provisional }) == action.callUUID {
            // The agent declined before the invite reached the domain: the invite is dropped when it arrives.
            endProvisionalSilently()
        }
        action.fulfill()
    }

    func provider(_ provider: CXProvider, perform action: CXSetHeldCallAction) {
        if let id = callId(for: action.callUUID) {
            if action.isOnHold {
                currentListener?.onSetInactiveRequested(callId: id)
            } else {
                currentListener?.onSetActiveRequested(callId: id)
            }
        }
        action.fulfill()
    }

    func provider(_ provider: CXProvider, perform action: CXSetMutedCallAction) {
        if let id = callId(for: action.callUUID) {
            currentListener?.onMuteChanged(callId: id, muted: action.isMuted)
        }
        action.fulfill()
    }

    func provider(_ provider: CXProvider, perform action: CXPlayDTMFCallAction) {
        onDTMF?(action.digits)
        action.fulfill()
    }

    func provider(_ provider: CXProvider, didActivate audioSession: AVAudioSession) {
        onAudioSessionChanged?(true)
        publishRoutes()
    }

    func provider(_ provider: CXProvider, didDeactivate audioSession: AVAudioSession) {
        onAudioSessionChanged?(false)
    }

    func provider(_ provider: CXProvider, timedOutPerforming action: CXAction) {
        action.fail()
    }

    private func endProvisionalSilently() {
        lock.withLock {
            provisionalTimeout?.cancel()
            provisionalTimeout = nil
            provisional = nil
        }
    }
}
