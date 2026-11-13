import SwiftUI

/// Button variants from the design system. Use only one `.primary` per screen.
enum TalkdeskButtonVariant {
    /// `action-primary` fill. The single main action of a screen.
    case primary
    /// `surface-1` with a `border-control` outline. Alternatives to the primary action.
    case secondary
    /// Borderless, `text-brand`. Low-emphasis actions in cards, toolbars and dialogs.
    case ghost
    /// Borderless, `text-info`.
    case ghostInfo
    /// Borderless, `text-error`.
    case ghostDanger
    /// `action-reject` fill. Only to confirm an irreversible action in a dialog.
    case destructive
}

enum TalkdeskButtonSize {
    case regular
    case small
}

/// Text button style: height 48 (small 36), radius 4, `bodyStrong` label (sentence case, no full stop).
///
/// ```swift
/// Button("Log in") { … }.buttonStyle(.talkdesk(.primary, fullWidth: true))
/// ```
struct TalkdeskButtonStyle: ButtonStyle {
    var variant: TalkdeskButtonVariant = .primary
    var size: TalkdeskButtonSize = .regular
    var fullWidth: Bool = false
    /// Keeps the pressed fill while the button shows its loading spinner.
    var isLoading: Bool = false

    func makeBody(configuration: Configuration) -> some View {
        StyledBody(configuration: configuration, variant: variant, size: size, fullWidth: fullWidth, isLoading: isLoading)
    }

    private struct StyledBody: View {
        let configuration: Configuration
        let variant: TalkdeskButtonVariant
        let size: TalkdeskButtonSize
        let fullWidth: Bool
        let isLoading: Bool
        @Environment(\.isEnabled) private var isEnabled

        var body: some View {
            let colors = resolvedColors
            let isGhost = [.ghost, .ghostInfo, .ghostDanger].contains(variant)
            let shape = RoundedRectangle(cornerRadius: TalkdeskRadius.sm)
            var labelStyle = TalkdeskTypography.bodyStrong
            if size == .small {
                labelStyle = TalkdeskTextStyle(size: 14, lineHeight: 20, weight: .medium, tracking: 0.1, relativeTo: .subheadline)
            }
            return configuration.label
                .talkdeskText(labelStyle)
                .lineLimit(1)
                .foregroundStyle(colors.content)
                .padding(.horizontal, size == .small || isGhost ? TalkdeskSpacing.s12 : TalkdeskSpacing.s20)
                .frame(maxWidth: fullWidth ? .infinity : nil, minHeight: size == .small ? 36 : TalkdeskSizes.touch)
                .background(colors.container, in: shape)
                .overlay {
                    if let border = colors.border { shape.strokeBorder(border, lineWidth: 1) }
                }
                .contentShape(shape)
        }

        private var resolvedColors: (container: Color, content: Color, border: Color?) {
            let pressed = configuration.isPressed || isLoading
            switch variant {
            case .primary:
                if !isEnabled { return (TalkdeskColors.actionPrimaryDisabled, TalkdeskColors.textOnDisabled, nil) }
                return (pressed ? TalkdeskColors.actionPrimaryPressed : TalkdeskColors.actionPrimary, TalkdeskColors.textOnBrand, nil)
            case .secondary:
                if !isEnabled { return (TalkdeskColors.surface1, TalkdeskColors.textDisabled, TalkdeskColors.borderDivider) }
                return (pressed ? TalkdeskColors.surfaceSunken : TalkdeskColors.surface1, TalkdeskColors.textDefault, TalkdeskColors.borderControl)
            case .ghost, .ghostInfo, .ghostDanger:
                let content: Color = switch variant {
                case _ where !isEnabled: TalkdeskColors.textDisabled
                case .ghostInfo: TalkdeskColors.textInfo
                case .ghostDanger: TalkdeskColors.textError
                default: TalkdeskColors.textBrand
                }
                return (pressed && isEnabled ? TalkdeskColors.surfaceSelected : .clear, content, nil)
            case .destructive:
                // The design system defines no pressed/disabled colours for destructive; disabled reuses primary's.
                if !isEnabled { return (TalkdeskColors.actionPrimaryDisabled, TalkdeskColors.textOnDisabled, nil) }
                return (TalkdeskColors.actionReject, TalkdeskColors.textOnFill, nil)
            }
        }
    }
}

extension ButtonStyle where Self == TalkdeskButtonStyle {
    /// Talkdesk text button style.
    static func talkdesk(
        _ variant: TalkdeskButtonVariant = .primary,
        size: TalkdeskButtonSize = .regular,
        fullWidth: Bool = false,
        isLoading: Bool = false
    ) -> TalkdeskButtonStyle {
        TalkdeskButtonStyle(variant: variant, size: size, fullWidth: fullWidth, isLoading: isLoading)
    }
}

/// Convenience text button with an optional leading 20pt icon.
///
/// `isLoading` shows an 18pt spinner before the title (use a progress label such as
/// "Opening sign-in…"), keeps the pressed fill and the same footprint, and ignores taps. The
/// spinner is static when Reduce Motion is on.
struct TalkdeskButton: View {
    let title: LocalizedStringKey
    var variant: TalkdeskButtonVariant = .primary
    var size: TalkdeskButtonSize = .regular
    var fullWidth: Bool = false
    var icon: Image?
    var isLoading: Bool = false
    let action: () -> Void

    init(
        _ title: LocalizedStringKey,
        variant: TalkdeskButtonVariant = .primary,
        size: TalkdeskButtonSize = .regular,
        fullWidth: Bool = false,
        icon: Image? = nil,
        isLoading: Bool = false,
        action: @escaping () -> Void
    ) {
        self.title = title
        self.variant = variant
        self.size = size
        self.fullWidth = fullWidth
        self.icon = icon
        self.isLoading = isLoading
        self.action = action
    }

    var body: some View {
        Button(action: action) {
            HStack(spacing: isLoading ? TalkdeskSpacing.s12 : TalkdeskSpacing.s8) {
                if isLoading {
                    ButtonSpinner()
                } else if let icon {
                    icon.resizable().frame(width: TalkdeskSizes.iconSmall, height: TalkdeskSizes.iconSmall)
                }
                Text(title)
            }
        }
        .buttonStyle(.talkdesk(variant, size: size, fullWidth: fullWidth, isLoading: isLoading))
        .allowsHitTesting(!isLoading)
        .accessibilityAddTraits(isLoading ? .updatesFrequently : [])
    }
}

/// 18pt spinner for the button loading state: a 2pt ring at 40% of the label colour with a
/// rotating quarter in the label colour.
private struct ButtonSpinner: View {
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @State private var rotation: Double = 0

    var body: some View {
        ZStack {
            Circle().stroke(.primary.opacity(0.4), lineWidth: 2)
            Circle()
                .trim(from: 0, to: 0.25)
                .stroke(.primary, lineWidth: 2)
                .rotationEffect(.degrees(rotation - 135))
        }
        .padding(1)
        .frame(width: 18, height: 18)
        .onAppear {
            guard !reduceMotion else { return }
            withAnimation(.linear(duration: 0.8).repeatForever(autoreverses: false)) { rotation = 360 }
        }
        .accessibilityHidden(true)
    }
}

/// Inline text link (`ta-link`): 14/500 `text-brand`, underlined. Use for "Show more" inside cards.
struct TalkdeskLink: View {
    let title: LocalizedStringKey
    let action: () -> Void
    @Environment(\.isEnabled) private var isEnabled

    init(_ title: LocalizedStringKey, action: @escaping () -> Void) {
        self.title = title
        self.action = action
    }

    var body: some View {
        Button(action: action) {
            Text(title)
                .underline()
                .talkdeskText(TalkdeskTypography.label)
                .foregroundStyle(isEnabled ? TalkdeskColors.textBrand : TalkdeskColors.textDisabled)
                .frame(minHeight: 24)
        }
        .buttonStyle(.plain)
    }
}

#Preview("Buttons") {
    VStack(alignment: .leading, spacing: 12) {
        HStack {
            TalkdeskButton("Log in") {}
            TalkdeskButton("Disabled") {}.disabled(true)
        }
        HStack {
            TalkdeskButton("Transfer", variant: .secondary, icon: TalkdeskIcons.transfer) {}
            TalkdeskButton("Disabled", variant: .secondary) {}.disabled(true)
        }
        HStack {
            TalkdeskButton("Show more", variant: .ghost) {}
            TalkdeskButton("Retry", variant: .ghostInfo) {}
            TalkdeskButton("Delete", variant: .ghostDanger) {}
        }
        HStack {
            TalkdeskButton("End for all", variant: .destructive) {}
            TalkdeskButton("Small", size: .small) {}
        }
        TalkdeskButton("Continue", fullWidth: true) {}
        TalkdeskButton("Opening sign-in…", fullWidth: true, isLoading: true) {}
        TalkdeskLink("Show more") {}
    }
    .padding()
    .talkdeskTheme()
}
