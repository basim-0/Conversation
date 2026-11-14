import ConversationKit
import SwiftUI

/// The in-app call screen for Calling, On call and On hold, with the call-quality indicator while connected and the
/// in-call keypad morphing over the controls. Incoming calls never reach here until answered: CallKit is the incoming
/// screen on iOS. Content (name, rings, timer) is solid; the controls float over it as Liquid Glass, and active
/// toggles are solid `control-selected`. Renders the shared `CallViewModel` state and sends its intents.
struct CallView: View {
    @StateObject private var model: CallModel

    init(onMinimize: @escaping () -> Void) {
        _model = StateObject(wrappedValue: CallModel(onMinimize: onMinimize))
    }

    var body: some View {
        let state = model.state
        ZStack {
            TalkdeskColors.bgWindow.ignoresSafeArea()
            body(state)
            if state.isKeypadOpen {
                keypad(state)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .animation(.easeInOut(duration: 0.3), value: state.isKeypadOpen)
        .animation(.easeOut(duration: 0.15), value: state.phase)
        .sensoryFeedback(.selection, trigger: state.isMuted)
        .sensoryFeedback(.selection, trigger: state.phase == .onHold)
        .task { await model.observeState() }
        .task { await model.observeEffects() }
    }

    // MARK: Call body

    private func body(_ state: CallState) -> some View {
        VStack(spacing: 0) {
            HStack {
                Button(action: model.minimize) {
                    TalkdeskIcons.chevDown
                        .resizable()
                        .frame(width: TalkdeskSizes.icon, height: TalkdeskSizes.icon)
                        .foregroundStyle(TalkdeskColors.iconDefault)
                        .frame(width: 44, height: 44)
                        .talkdeskGlass(in: Circle())
                }
                .accessibilityLabel("Minimize call")
                Spacer()
                if state.phase == .onCall, let quality = state.quality {
                    TalkdeskCallQualityIndicator(quality: quality.designSystem, label: quality.label)
                        .accessibilityLabel(Text("Call quality: \(quality.spokenName)"))
                }
            }
            .frame(height: TalkdeskSizes.toolbar)
            .padding(.horizontal, TalkdeskSpacing.s16)

            header(state)
                .padding(.top, TalkdeskSpacing.s8)
            Spacer(minLength: TalkdeskSpacing.s16)
            controls(state)
                .padding(.horizontal, TalkdeskSpacing.s40)
            TalkdeskCallButton(.end, caption: "End", accessibilityLabel: "End call") {
                UIImpactFeedbackGenerator(style: .heavy).impactOccurred()
                model.endCall()
            }
            .frame(height: 152)
        }
    }

    private func header(_ state: CallState) -> some View {
        VStack(spacing: TalkdeskSpacing.s8) {
            switch state.phase {
            case .calling:
                TalkdeskCallStateLabel(text: "Calling…", icon: TalkdeskIcons.phoneOut, color: TalkdeskColors.textInfo)
            case .onCall:
                TalkdeskCallStateLabel(text: "On call", icon: TalkdeskIcons.phone, color: TalkdeskColors.textSuccess)
            case .onHold:
                onHoldChip
            }
            switch state.phase {
            case .onHold:
                TalkdeskAvatar(initials: state.contact.initials, size: .large)
                    .opacity(0.6)
                    .frame(width: 168, height: 168)
                    .padding(.vertical, TalkdeskSpacing.s12)
            default:
                TalkdeskCallRings(state: state.phase == .calling ? .dialing : .onCall) {
                    TalkdeskAvatar(initials: state.contact.initials, size: .large)
                }
                .padding(.vertical, TalkdeskSpacing.s16)
            }
            Text(state.contact.name ?? state.contact.number)
                .talkdeskText(TalkdeskTextStyle(size: 24, lineHeight: 30, weight: .medium, tracking: 0, relativeTo: .title2))
                .foregroundStyle(TalkdeskColors.textDefault)
                .lineLimit(1)
                .minimumScaleFactor(0.7)
                .padding(.horizontal, TalkdeskSpacing.s24)
            if state.phase == .calling {
                Text(phoneLine(state.contact))
                    .talkdeskText(TalkdeskTypography.body)
                    .foregroundStyle(TalkdeskColors.textDescription)
            } else {
                Text(formatElapsed(Int(state.elapsedSeconds)))
                    .talkdeskText(TalkdeskTypography.timer)
                    .foregroundStyle(TalkdeskColors.textDescription)
            }
        }
        .accessibilityElement(children: .combine)
    }

    private var onHoldChip: some View {
        HStack(spacing: 6) {
            TalkdeskIcons.pause
                .resizable()
                .frame(width: TalkdeskSizes.iconSmall, height: TalkdeskSizes.iconSmall)
            Text("On hold").talkdeskText(TalkdeskTypography.label)
        }
        .foregroundStyle(TalkdeskColors.textWarning)
        .padding(.horizontal, TalkdeskSpacing.s12)
        .frame(height: 32)
        .background(TalkdeskColors.fillWarningSubtle, in: Capsule())
    }

    private func phoneLine(_ contact: CallContact) -> String {
        if let type = contact.phoneType, !type.isEmpty, contact.name != nil {
            return "\(type) · \(contact.number)"
        }
        return contact.name == nil ? "" : contact.number
    }

    /// Mute, Hold, Speaker and Keypad in a 2x2 grid. Active toggles flip their caption to the action that undoes them.
    /// Hold and Keypad are disabled until the call connects.
    private func controls(_ state: CallState) -> some View {
        let onHold = state.phase == .onHold
        let connected = state.canUseConnectedControls
        return VStack(spacing: 20) {
            HStack(spacing: 24) {
                TalkdeskCallControl(
                    icon: state.isMuted ? TalkdeskIcons.micOff : TalkdeskIcons.mic,
                    label: state.isMuted ? "Unmute" : "Mute",
                    active: state.isMuted,
                    action: model.toggleMute
                )
                .frame(maxWidth: .infinity)
                TalkdeskCallControl(
                    icon: onHold ? TalkdeskIcons.play : TalkdeskIcons.pause,
                    label: onHold ? "Resume" : "Hold",
                    active: onHold,
                    enabled: connected,
                    action: model.toggleHold
                )
                .frame(maxWidth: .infinity)
            }
            HStack(spacing: 24) {
                TalkdeskCallControl(
                    icon: TalkdeskIcons.speaker,
                    label: "Speaker",
                    active: state.isSpeakerOn,
                    action: model.toggleSpeaker
                )
                .frame(maxWidth: .infinity)
                TalkdeskCallControl(
                    icon: TalkdeskIcons.dialpad,
                    label: "Keypad",
                    active: false,
                    enabled: connected,
                    action: model.showKeypad
                )
                .frame(maxWidth: .infinity)
            }
        }
    }

    // MARK: Keypad

    private func keypad(_ state: CallState) -> some View {
        VStack(spacing: 0) {
            HStack(spacing: TalkdeskSpacing.s12) {
                TalkdeskAvatar(initials: state.contact.initials)
                VStack(alignment: .leading, spacing: 0) {
                    Text(state.contact.name ?? state.contact.number)
                        .talkdeskText(TalkdeskTypography.bodyStrong)
                        .foregroundStyle(TalkdeskColors.textDefault)
                        .lineLimit(1)
                    Text("\(state.phase == .onHold ? String(localized: "On hold") : String(localized: "On call")) · \(formatElapsed(Int(state.elapsedSeconds)))")
                        .talkdeskText(TalkdeskTypography.labelRegular)
                        .foregroundStyle(TalkdeskColors.textSuccess)
                }
                Spacer(minLength: 0)
            }
            .frame(height: 64)
            .padding(.horizontal, TalkdeskSpacing.s16)

            Text(state.dtmfDigits.map(String.init).joined(separator: " "))
                .talkdeskText(TalkdeskTextStyle(size: 28, lineHeight: 34, weight: .regular, tracking: 4, relativeTo: .title1))
                .foregroundStyle(TalkdeskColors.textDefault)
                .frame(maxWidth: .infinity, minHeight: 34)
                .padding(.top, TalkdeskSpacing.s32)
                .padding(.horizontal, TalkdeskSpacing.s16)
                .accessibilityLabel(Text(state.dtmfDigits))
                .accessibilityAddTraits(.updatesFrequently)

            Spacer(minLength: TalkdeskSpacing.s16)
            TalkdeskDialpad(onKey: model.sendDigit)
                .padding(.horizontal, TalkdeskSpacing.s24)
            HStack {
                Color.clear.frame(maxWidth: .infinity)
                TalkdeskCallButton(.end, caption: "End", accessibilityLabel: "End call", action: model.endCall)
                    .frame(maxWidth: .infinity)
                Button("Hide", action: model.hideKeypad)
                    .buttonStyle(.talkdesk(.ghost))
                    .frame(maxWidth: .infinity)
            }
            .frame(height: 152)
            .padding(.horizontal, TalkdeskSpacing.s24)
        }
        .background(TalkdeskColors.bgWindow)
    }
}

private extension CallQuality {
    var designSystem: TalkdeskCallQuality {
        switch self {
        case .excellent: .excellent
        case .good: .good
        case .poor: .poor
        }
    }

    var label: LocalizedStringKey {
        switch self {
        case .excellent: "Excellent"
        case .good: "Good"
        case .poor: "Poor"
        }
    }

    var spokenName: String {
        switch self {
        case .excellent: String(localized: "Excellent")
        case .good: String(localized: "Good")
        case .poor: String(localized: "Poor")
        }
    }
}
