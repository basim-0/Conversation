import SwiftUI

// Call components of the design system (`ta-dialpad`, `ta-callbtn`, `ta-ctrl`, `ta-rings`, `ta-callstate`,
// `ta-quality`, `ta-avatar`): the iOS counterparts of Android's `TalkdeskDialpad`, `TalkdeskCallButton`,
// `TalkdeskCallControl`, `TalkdeskCallRings`, `TalkdeskCallStateLabel`, `TalkdeskCallQualityIndicator` and
// `TalkdeskAvatar`, with the same token names and sizes.

// MARK: - Avatar

enum TalkdeskAvatarSize {
    case regular
    case large

    var diameter: CGFloat { self == .large ? TalkdeskSizes.avatarLarge : TalkdeskSizes.avatar }
}

/// Initials disc (`ta-avatar`): `surface-brand-subtle` with `text-brand` initials, 40pt (96pt large, for call and
/// profile screens). Without initials it shows the person icon.
struct TalkdeskAvatar: View {
    let initials: String?
    var size: TalkdeskAvatarSize = .regular

    var body: some View {
        let large = size == .large
        ZStack {
            Circle().fill(TalkdeskColors.surfaceBrandSubtle)
            if let initials, !initials.isEmpty {
                Text(initials)
                    .talkdeskText(large
                        ? TalkdeskTextStyle(size: 32, lineHeight: 40, weight: .medium, tracking: 0, relativeTo: .largeTitle)
                        : TalkdeskTypography.label)
                    .foregroundStyle(TalkdeskColors.textBrand)
            } else {
                TalkdeskIcons.user
                    .resizable()
                    .frame(width: large ? 40 : 20, height: large ? 40 : 20)
                    .foregroundStyle(TalkdeskColors.textBrand)
            }
        }
        .frame(width: size.diameter, height: size.diameter)
        .accessibilityHidden(true)
    }
}

// MARK: - Glass

extension View {
    /// Liquid Glass on iOS 26+, a thin material below it. Used for controls that float over content (the call
    /// controls, the sheet's close button); content stays on solid surfaces.
    @ViewBuilder
    func talkdeskGlass<S: InsettableShape>(in shape: S, interactive: Bool = true) -> some View {
        if #available(iOS 26.0, *) {
            self.glassEffect(interactive ? .regular.interactive() : .regular, in: shape)
        } else {
            self.background(.ultraThinMaterial, in: shape)
                .overlay { shape.strokeBorder(TalkdeskColors.borderDivider, lineWidth: 1) }
        }
    }
}

// MARK: - Dialpad

private struct DialKey: Identifiable {
    let digit: Character
    let letters: String
    var id: Character { digit }
}

private let dialKeys: [[DialKey]] = [
    [DialKey(digit: "1", letters: ""), DialKey(digit: "2", letters: "ABC"), DialKey(digit: "3", letters: "DEF")],
    [DialKey(digit: "4", letters: "GHI"), DialKey(digit: "5", letters: "JKL"), DialKey(digit: "6", letters: "MNO")],
    [DialKey(digit: "7", letters: "PQRS"), DialKey(digit: "8", letters: "TUV"), DialKey(digit: "9", letters: "WXYZ")],
    [DialKey(digit: "*", letters: ""), DialKey(digit: "0", letters: "+"), DialKey(digit: "#", letters: "")],
]

/// The 3x4 key grid of the dialer and the in-call keypad (`ta-dialpad`). Keys are 64pt high on `surface-key` with a
/// `border-divider` outline and `radius-sm`, 10pt apart; pressed `surface-sunken`. `onKey` gets the digit; a long
/// press on 0 sends "+" instead. Each tap gives a soft haptic.
struct TalkdeskDialpad: View {
    let onKey: (Character) -> Void

    var body: some View {
        VStack(spacing: 10) {
            ForEach(Array(dialKeys.enumerated()), id: \.offset) { _, row in
                HStack(spacing: 10) {
                    ForEach(row) { key in
                        DialpadKey(key: key, onKey: onKey)
                    }
                }
            }
        }
    }
}

private struct DialpadKey: View {
    let key: DialKey
    let onKey: (Character) -> Void
    @GestureState private var pressed = false
    @State private var longPressed = false

    var body: some View {
        let shape = RoundedRectangle(cornerRadius: TalkdeskRadius.sm)
        VStack(spacing: 0) {
            Text(String(key.digit))
                .talkdeskText(TalkdeskTypography.numberDisplay)
                .foregroundStyle(TalkdeskColors.textDefault)
            // The letters line is always laid out so every key has the same text block height.
            Text(key.letters.isEmpty ? " " : key.letters)
                .talkdeskText(TalkdeskTypography.navLabel)
                .tracking(1)
                .foregroundStyle(TalkdeskColors.textDescription)
        }
        .frame(maxWidth: .infinity)
        .frame(height: TalkdeskSizes.dialpadKey)
        .background(pressed ? TalkdeskColors.surfaceSunken : TalkdeskColors.surfaceKey, in: shape)
        .overlay { shape.strokeBorder(TalkdeskColors.borderDivider, lineWidth: 1) }
        .contentShape(shape)
        .gesture(
            DragGesture(minimumDistance: 0)
                .updating($pressed) { _, state, _ in state = true }
                .onEnded { value in
                    // A drag that left the key is a cancelled tap.
                    guard abs(value.translation.width) < TalkdeskSizes.dialpadKey,
                          abs(value.translation.height) < TalkdeskSizes.dialpadKey else { return }
                    if longPressed {
                        longPressed = false
                        return
                    }
                    UIImpactFeedbackGenerator(style: .soft).impactOccurred()
                    onKey(key.digit)
                }
        )
        .simultaneousGesture(
            LongPressGesture(minimumDuration: 0.5).onEnded { _ in
                guard key.digit == "0" else { return }
                longPressed = true
                UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                onKey("+")
            }
        )
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(String(key.digit))
        .accessibilityAddTraits(.isButton)
        .accessibilityAction { onKey(key.digit) }
    }
}

// MARK: - Call buttons

enum TalkdeskCallButtonKind {
    /// `action-call`. Start or accept a call.
    case call
    /// `action-reject`. Decline an incoming call.
    case reject
    /// `action-end`. Hang up.
    case end
}

/// Circular call button (`ta-callbtn`): 64pt, `text-on-fill` icon. Pressed Call uses `action-call-pressed`;
/// disabled is 38%.
struct TalkdeskCallButtonStyle: ButtonStyle {
    let kind: TalkdeskCallButtonKind
    var size: CGFloat = TalkdeskSizes.callButton
    @Environment(\.isEnabled) private var isEnabled

    func makeBody(configuration: Configuration) -> some View {
        let container: Color = switch kind {
        case .call: configuration.isPressed ? TalkdeskColors.actionCallPressed : TalkdeskColors.actionCall
        case .reject: TalkdeskColors.actionReject
        case .end: TalkdeskColors.actionEnd
        }
        return configuration.label
            .foregroundStyle(TalkdeskColors.textOnFill)
            .frame(width: size, height: size)
            .background(container, in: Circle())
            .opacity(isEnabled ? 1 : 0.38)
            .scaleEffect(configuration.isPressed ? 0.96 : 1)
            .animation(.easeOut(duration: 0.1), value: configuration.isPressed)
    }
}

/// `TalkdeskCallButtonStyle` button with its visible caption underneath (`ta-callbtn-wrap`, 13/18 Medium, 8pt gap).
/// The caption is required on call screens so call actions never rely on colour alone.
struct TalkdeskCallButton: View {
    let kind: TalkdeskCallButtonKind
    let caption: LocalizedStringKey?
    let accessibilityLabel: LocalizedStringKey
    var icon: Image?
    var size: CGFloat = TalkdeskSizes.callButton
    let action: () -> Void

    init(
        _ kind: TalkdeskCallButtonKind,
        caption: LocalizedStringKey? = nil,
        accessibilityLabel: LocalizedStringKey,
        icon: Image? = nil,
        size: CGFloat = TalkdeskSizes.callButton,
        action: @escaping () -> Void
    ) {
        self.kind = kind
        self.caption = caption
        self.accessibilityLabel = accessibilityLabel
        self.icon = icon
        self.size = size
        self.action = action
    }

    var body: some View {
        VStack(spacing: TalkdeskSpacing.s8) {
            Button(action: action) {
                (icon ?? (kind == .call ? TalkdeskIcons.phone : TalkdeskIcons.phoneEnd))
                    .resizable()
                    .frame(width: TalkdeskSizes.icon, height: TalkdeskSizes.icon)
            }
            .buttonStyle(TalkdeskCallButtonStyle(kind: kind, size: size))
            .accessibilityLabel(accessibilityLabel)
            if let caption {
                Text(caption)
                    .talkdeskText(TalkdeskTextStyle(size: 13, lineHeight: 18, weight: .medium, tracking: 0.1, relativeTo: .footnote))
                    .foregroundStyle(TalkdeskColors.textDefault)
                    .accessibilityHidden(true)
            }
        }
    }
}

/// On-call utility control (`ta-ctrl`): a 56pt toggle with a caption underneath. The disc floats over the call, so
/// inactive is Liquid Glass (thin material below iOS 26); active (muted, on hold, speaker on) is a solid
/// `control-selected` disc so state reads at a glance and meets contrast. Pass the caption that undoes the action
/// ("Unmute", "Resume") as `label` when active. Disabled is 38%.
struct TalkdeskCallControl: View {
    let icon: Image
    let label: LocalizedStringKey
    let active: Bool
    var enabled: Bool = true
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            VStack(spacing: TalkdeskSpacing.s8) {
                disc
                Text(label)
                    .talkdeskText(TalkdeskTextStyle(size: 13, lineHeight: 18, weight: .medium, tracking: 0.1, relativeTo: .footnote))
                    .foregroundStyle(TalkdeskColors.textDefault)
            }
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .disabled(!enabled)
        .opacity(enabled ? 1 : 0.38)
        .accessibilityElement(children: .combine)
        .accessibilityAddTraits(active ? [.isButton, .isSelected] : .isButton)
    }

    @ViewBuilder
    private var disc: some View {
        let glyph = icon
            .resizable()
            .frame(width: TalkdeskSizes.icon, height: TalkdeskSizes.icon)
            .foregroundStyle(active ? TalkdeskColors.controlOnSelected : TalkdeskColors.iconDefault)
            .frame(width: TalkdeskSizes.control, height: TalkdeskSizes.control)
        if active {
            glyph
                .background(TalkdeskColors.controlSelected, in: Circle())
                .animation(.easeOut(duration: 0.15), value: active)
        } else {
            glyph
                .talkdeskGlass(in: Circle(), interactive: false)
                .animation(.easeOut(duration: 0.15), value: active)
        }
    }
}

// MARK: - Rings, state label, quality

enum TalkdeskCallState {
    case incoming, dialing, onCall

    var color: Color {
        switch self {
        case .incoming: TalkdeskColors.callstateIncoming
        case .dialing: TalkdeskColors.callstateDialing
        case .onCall: TalkdeskColors.callstateOncall
        }
    }
}

/// Pulse rings (`ta-rings`): three concentric discs in the call-state colour at 12/24/40% opacity around `content`
/// (the avatar). Dialing breathes (1600ms, staggered 200ms) and stays still under Reduce Motion; on-call is two static
/// 12% halos (the outer one and one 16pt inside it), as on the design board.
struct TalkdeskCallRings<Content: View>: View {
    let state: TalkdeskCallState
    var size: CGFloat = 168
    var pulsing: Bool = true
    @ViewBuilder let content: () -> Content

    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @State private var breathing = false

    var body: some View {
        ZStack {
            if state == .onCall {
                Circle().fill(state.color).opacity(0.12)
                Circle().fill(state.color).opacity(0.12).frame(width: size - 32, height: size - 32)
            } else {
                // Diameter of each disc as a share of `size`, with its opacity and delay: outer, middle, inner.
                ring(share: 1, opacity: 0.12, delay: 0)
                ring(share: 0.81, opacity: 0.24, delay: 0.2)
                ring(share: 0.67, opacity: 0.40, delay: 0.4)
            }
            content()
        }
        .frame(width: size, height: size)
        .onAppear { breathing = true }
    }

    private func ring(share: CGFloat, opacity: Double, delay: Double) -> some View {
        let animated = pulsing && !reduceMotion
        return Circle()
            .fill(state.color)
            .opacity(opacity)
            .frame(width: size * share, height: size * share)
            .scaleEffect(animated && breathing ? 1.06 : 1)
            .animation(
                animated ? .easeInOut(duration: 1.6).repeatForever(autoreverses: true).delay(delay) : nil,
                value: breathing
            )
    }
}

/// State label (`ta-callstate--*`): icon and word in the matching text colour, always shown next to the rings.
struct TalkdeskCallStateLabel: View {
    let text: LocalizedStringKey
    let icon: Image
    let color: Color

    var body: some View {
        HStack(spacing: 6) {
            icon
                .resizable()
                .frame(width: TalkdeskSizes.iconSmall, height: TalkdeskSizes.iconSmall)
            Text(text).talkdeskText(TalkdeskTypography.label)
        }
        .foregroundStyle(color)
        .accessibilityElement(children: .combine)
    }
}

enum TalkdeskCallQuality {
    case excellent, good, poor
}

/// Call quality (`ta-quality`): three bars (6/10/14pt) plus a word. Bars are `quality-excellent`, `quality-good` or
/// `quality-bad` for the filled ones and `track` for the rest.
struct TalkdeskCallQualityIndicator: View {
    let quality: TalkdeskCallQuality
    let label: LocalizedStringKey

    var body: some View {
        let (filled, fill): (Int, Color) = switch quality {
        case .excellent: (3, TalkdeskColors.qualityExcellent)
        case .good: (2, TalkdeskColors.qualityGood)
        case .poor: (1, TalkdeskColors.qualityBad)
        }
        HStack(alignment: .bottom, spacing: 6) {
            HStack(alignment: .bottom, spacing: 2) {
                ForEach(Array([6, 10, 14].enumerated()), id: \.offset) { index, height in
                    RoundedRectangle(cornerRadius: TalkdeskRadius.xs)
                        .fill(index < filled ? fill : TalkdeskColors.track)
                        .frame(width: 4, height: CGFloat(height))
                }
            }
            .accessibilityHidden(true)
            Text(label)
                .talkdeskText(TalkdeskTypography.caption)
                .foregroundStyle(TalkdeskColors.textDescription)
        }
        .accessibilityElement(children: .combine)
    }
}

#Preview("Call components") {
    VStack(spacing: 20) {
        HStack(spacing: 24) {
            TalkdeskCallRings(state: .dialing, size: 120, pulsing: false) { TalkdeskAvatar(initials: "MS", size: .large) }
            TalkdeskCallRings(state: .onCall, size: 120) { TalkdeskAvatar(initials: "MS", size: .large) }
        }
        TalkdeskCallStateLabel(text: "On call", icon: TalkdeskIcons.phone, color: TalkdeskColors.textSuccess)
        TalkdeskCallQualityIndicator(quality: .good, label: "Good")
        HStack(spacing: 24) {
            TalkdeskCallControl(icon: TalkdeskIcons.mic, label: "Mute", active: false) {}
            TalkdeskCallControl(icon: TalkdeskIcons.micOff, label: "Unmute", active: true) {}
        }
        HStack(spacing: 48) {
            TalkdeskCallButton(.reject, caption: "Decline", accessibilityLabel: "Decline") {}
            TalkdeskCallButton(.call, caption: "Accept", accessibilityLabel: "Accept") {}
        }
        TalkdeskDialpad { _ in }.padding(.horizontal, 16)
    }
    .padding()
    .talkdeskTheme()
}
