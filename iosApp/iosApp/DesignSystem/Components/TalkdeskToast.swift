import SwiftUI

enum TalkdeskToastVariant {
    case success, primary, error, warning, info, neutral

    var colors: (container: Color, content: Color) {
        switch self {
        case .success: (TalkdeskColors.fillSuccess, TalkdeskColors.textOnFill)
        case .primary: (TalkdeskColors.actionPrimary, TalkdeskColors.textOnBrand)
        case .error: (TalkdeskColors.fillError, TalkdeskColors.textOnFill)
        case .warning: (TalkdeskColors.fillWarning, TalkdeskColors.textOnLightFill)
        case .info: (TalkdeskColors.fillInfo, TalkdeskColors.textOnFill)
        case .neutral: (TalkdeskColors.surface2, TalkdeskColors.textDefault)
        }
    }

    var defaultIcon: Image {
        switch self {
        case .success: TalkdeskIcons.check
        case .error, .warning: TalkdeskIcons.alert
        case .primary, .info, .neutral: TalkdeskIcons.info
        }
    }
}

/// Transient feedback (`ta-toast`): radius 4, `shadow-overlay`, at most 360pt wide, one optional
/// action. Every toast carries an icon and words, never colour alone. Present it with `.talkdeskToast(_:)`.
struct TalkdeskToast: View {
    let message: LocalizedStringKey
    var variant: TalkdeskToastVariant = .neutral
    var icon: Image?
    var actionLabel: LocalizedStringKey?
    var action: (() -> Void)?

    var body: some View {
        let colors = variant.colors
        let shape = RoundedRectangle(cornerRadius: TalkdeskRadius.sm)
        HStack(spacing: TalkdeskSpacing.s12) {
            (icon ?? variant.defaultIcon)
                .resizable()
                .frame(width: TalkdeskSizes.icon, height: TalkdeskSizes.icon)
                .accessibilityHidden(true)
            Text(message)
                .talkdeskText(TalkdeskTypography.body)
                .frame(maxWidth: .infinity, alignment: .leading)
            if let actionLabel, let action {
                Button(action: action) {
                    Text(actionLabel)
                        .underline()
                        .talkdeskText(TalkdeskTypography.bodyStrong)
                        .padding(.horizontal, TalkdeskSpacing.s8)
                        .frame(minHeight: 36)
                }
                .buttonStyle(.plain)
            }
        }
        .foregroundStyle(colors.content)
        .padding(.horizontal, TalkdeskSpacing.s16)
        .padding(.vertical, TalkdeskSpacing.s12)
        .frame(maxWidth: 360, minHeight: TalkdeskSizes.touch)
        .background(colors.container, in: shape)
        .overlay {
            if variant == .neutral { shape.strokeBorder(TalkdeskColors.borderDivider, lineWidth: 1) }
        }
        .talkdeskShadow(.overlay)
        .accessibilityElement(children: .combine)
    }
}

/// A toast to present with `.talkdeskToast(_:)`.
struct TalkdeskToastItem: Identifiable, Equatable {
    let id = UUID()
    let message: LocalizedStringKey
    var variant: TalkdeskToastVariant = .neutral
    var actionLabel: LocalizedStringKey?
    var action: (() -> Void)?

    /// Short toasts stay 4s; toasts with an action stay 10s, like Android's Short/Long.
    var duration: Duration { actionLabel == nil ? .seconds(4) : .seconds(10) }

    static func == (lhs: Self, rhs: Self) -> Bool { lhs.id == rhs.id }
}

private struct TalkdeskToastModifier: ViewModifier {
    @Binding var item: TalkdeskToastItem?

    func body(content: Content) -> some View {
        content.overlay(alignment: .bottom) {
            ZStack {
                if let toast = item {
                    TalkdeskToast(message: toast.message, variant: toast.variant, actionLabel: toast.actionLabel) {
                        toast.action?()
                        item = nil
                    }
                    .padding(TalkdeskSpacing.s16)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                    .task(id: toast.id) {
                        try? await Task.sleep(for: toast.duration)
                        if item?.id == toast.id { item = nil }
                    }
                }
            }
            .animation(.easeOut(duration: 0.2), value: item)
        }
    }
}

extension View {
    /// Shows `item` as a toast at the bottom of this view and clears it after its duration.
    func talkdeskToast(_ item: Binding<TalkdeskToastItem?>) -> some View {
        modifier(TalkdeskToastModifier(item: item))
    }
}

enum TalkdeskBannerVariant {
    case success, error, warning, info
}

/// Persistent inline message (`ta-banner`) on a subtle fill with the matching text colour. Use for
/// wrap-up timers and recording state. `title` is emphasised; `message` is in `text-default`.
struct TalkdeskBanner: View {
    let title: LocalizedStringKey
    var message: LocalizedStringKey?
    var variant: TalkdeskBannerVariant = .info
    var icon: Image?

    var body: some View {
        let (container, content): (Color, Color) = switch variant {
        case .success: (TalkdeskColors.fillSuccessSubtle, TalkdeskColors.textSuccess)
        case .error: (TalkdeskColors.fillErrorSubtle, TalkdeskColors.textError)
        case .warning: (TalkdeskColors.fillWarningSubtle, TalkdeskColors.textWarning)
        case .info: (TalkdeskColors.fillInfoSubtle, TalkdeskColors.textInfo)
        }
        let defaultIcon: Image = switch variant {
        case .success: TalkdeskIcons.check
        case .info: TalkdeskIcons.info
        case .error, .warning: TalkdeskIcons.alert
        }
        HStack(alignment: .top, spacing: TalkdeskSpacing.s12) {
            (icon ?? defaultIcon)
                .resizable()
                .frame(width: TalkdeskSizes.iconSmall, height: TalkdeskSizes.iconSmall)
                .foregroundStyle(content)
                .accessibilityHidden(true)
            VStack(alignment: .leading, spacing: 0) {
                Text(title)
                    .talkdeskText(TalkdeskTypography.label)
                    .foregroundStyle(content)
                if let message {
                    Text(message)
                        .talkdeskText(TalkdeskTypography.labelRegular)
                        .foregroundStyle(TalkdeskColors.textDefault)
                }
            }
            Spacer(minLength: 0)
        }
        .padding(.horizontal, TalkdeskSpacing.s16)
        .padding(.vertical, TalkdeskSpacing.s12)
        .background(container, in: RoundedRectangle(cornerRadius: TalkdeskRadius.sm))
        .accessibilityElement(children: .combine)
    }
}

#Preview("Toasts & banners") {
    VStack(spacing: 12) {
        TalkdeskToast(message: "Call transferred", variant: .success)
        TalkdeskToast(message: "Status set to Available", variant: .primary)
        TalkdeskToast(message: "Couldn't send message", variant: .error, actionLabel: "Retry") {}
        TalkdeskToast(message: "Weak connection", variant: .warning)
        TalkdeskToast(message: "Recording started", variant: .info)
        TalkdeskToast(message: "Copied to clipboard")
        TalkdeskBanner(title: "Wrap-up ends in 0:45", message: "Choose a disposition to finish.", variant: .warning)
        TalkdeskBanner(title: "Call is being recorded")
    }
    .padding()
    .talkdeskTheme()
}
