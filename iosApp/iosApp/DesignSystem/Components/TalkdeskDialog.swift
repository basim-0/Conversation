import SwiftUI

/// Centred modal card (`ta-dialog`): `surface-2`, radius 8, padding 24, 312pt wide on phones. Title in
/// `h3`, body in `text-description`, right-aligned actions with the confirming action last.
///
/// Actions are usually ghost `TalkdeskButton`s; permission dialogs pass an `icon` and use a primary
/// button to continue to the system prompt; irreversible actions use `.destructive`.
struct TalkdeskDialogCard<Actions: View>: View {
    let title: LocalizedStringKey
    var message: LocalizedStringKey?
    var icon: Image?
    @ViewBuilder var actions: () -> Actions

    var body: some View {
        VStack(alignment: .leading, spacing: TalkdeskSpacing.s16) {
            if let icon {
                icon
                    .resizable()
                    .frame(width: TalkdeskSizes.icon, height: TalkdeskSizes.icon)
                    .foregroundStyle(TalkdeskColors.iconBrand)
                    .frame(width: 40, height: 40)
                    .background(TalkdeskColors.surfaceBrandSubtle, in: Circle())
                    .accessibilityHidden(true)
            }
            Text(title)
                .talkdeskText(TalkdeskTypography.h3)
                .foregroundStyle(TalkdeskColors.textDefault)
                .accessibilityAddTraits(.isHeader)
            if let message {
                Text(message)
                    .talkdeskText(TalkdeskTypography.body)
                    .foregroundStyle(TalkdeskColors.textDescription)
            }
            HStack(spacing: TalkdeskSpacing.s8) {
                Spacer(minLength: 0)
                actions()
            }
        }
        .padding(TalkdeskSpacing.s24)
        .frame(maxWidth: 312, alignment: .leading)
        .background(TalkdeskColors.surface2, in: RoundedRectangle(cornerRadius: TalkdeskRadius.md))
        .talkdeskShadow(.overlay)
    }
}

private struct TalkdeskDialogModifier<Actions: View>: ViewModifier {
    @Binding var isPresented: Bool
    let title: LocalizedStringKey
    let message: LocalizedStringKey?
    let icon: Image?
    let dismissOnScrimTap: Bool
    @ViewBuilder let actions: () -> Actions

    func body(content: Content) -> some View {
        content.overlay {
            ZStack {
                if isPresented {
                    TalkdeskColors.scrim
                        .ignoresSafeArea()
                        .onTapGesture { if dismissOnScrimTap { isPresented = false } }
                        .accessibilityHidden(true)
                        .transition(.opacity)
                    TalkdeskDialogCard(title: title, message: message, icon: icon, actions: actions)
                        .padding(.horizontal, TalkdeskSpacing.s24)
                        .accessibilityAddTraits(.isModal)
                        .transition(.opacity.combined(with: .scale(scale: 0.95)))
                }
            }
            .animation(.easeOut(duration: 0.15), value: isPresented)
        }
    }
}

extension View {
    /// Presents a Talkdesk dialog over this view. Apply it at the screen root so the scrim covers the screen.
    func talkdeskDialog<Actions: View>(
        isPresented: Binding<Bool>,
        title: LocalizedStringKey,
        message: LocalizedStringKey? = nil,
        icon: Image? = nil,
        dismissOnScrimTap: Bool = true,
        @ViewBuilder actions: @escaping () -> Actions
    ) -> some View {
        modifier(TalkdeskDialogModifier(
            isPresented: isPresented, title: title, message: message, icon: icon,
            dismissOnScrimTap: dismissOnScrimTap, actions: actions
        ))
    }
}

#Preview("Dialog") {
    @Previewable @State var shown = true
    VStack {
        TalkdeskButton("Show dialog") { shown = true }
    }
    .frame(maxWidth: .infinity, maxHeight: .infinity)
    .talkdeskTheme()
    .talkdeskDialog(isPresented: $shown, title: "Allow microphone access",
                    message: "Talkdesk needs the microphone to make and receive calls.",
                    icon: TalkdeskIcons.mic) {
        TalkdeskButton("Not now", variant: .ghost) { shown = false }
        TalkdeskButton("Continue") { shown = false }
    }
}
