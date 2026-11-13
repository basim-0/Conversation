import SwiftUI

/// Centred empty or error state: 96pt `surface-brand-subtle` disc with a 40pt `icon-brand` icon, title
/// (18/24 semibold), body (15/21, `text-description`) and at most one pill action. Padded 40pt on the sides.
struct TalkdeskEmptyState: View {
    let icon: Image
    let title: LocalizedStringKey
    let message: LocalizedStringKey
    var actionTitle: LocalizedStringKey?
    var action: (() -> Void)?

    var body: some View {
        VStack(spacing: TalkdeskSpacing.s12) {
            icon
                .resizable()
                .frame(width: 40, height: 40)
                .foregroundStyle(TalkdeskColors.iconBrand)
                .frame(width: TalkdeskSizes.avatarLarge, height: TalkdeskSizes.avatarLarge)
                .background(TalkdeskColors.surfaceBrandSubtle, in: Circle())
                .padding(.bottom, TalkdeskSpacing.s8)
                .accessibilityHidden(true)
            Text(title)
                .talkdeskText(TalkdeskTypography.emptyTitle)
                .foregroundStyle(TalkdeskColors.textDefault)
                .multilineTextAlignment(.center)
            Text(message)
                .talkdeskText(TalkdeskTypography.body)
                .foregroundStyle(TalkdeskColors.textDescription)
                .multilineTextAlignment(.center)
            if let actionTitle, let action {
                Button(action: action) {
                    Text(actionTitle)
                        .talkdeskText(TalkdeskTypography.pillButton)
                        .foregroundStyle(TalkdeskColors.textBrand)
                        .padding(.horizontal, TalkdeskSpacing.s20)
                        .frame(minHeight: 44)
                        .background(TalkdeskColors.surfaceBrandSubtle, in: Capsule())
                        .contentShape(Capsule())
                }
                .buttonStyle(.plain)
                .padding(.top, TalkdeskSpacing.s4)
            }
        }
        .padding(.horizontal, TalkdeskSpacing.s40)
        .frame(maxWidth: .infinity)
        .accessibilityElement(children: .contain)
    }
}

#Preview("Empty state") {
    TalkdeskEmptyState(
        icon: TalkdeskIcons.mail,
        title: "No email activity",
        message: "Nothing matches your filter. Clear it to see all activity.",
        actionTitle: "Clear filters",
        action: {}
    )
    .talkdeskTheme()
}
