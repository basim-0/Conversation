import SwiftUI

/// Grouped information (`ta-card`): `surface-1`, 1pt `border-divider`, radius 8, padding 16, gap 12,
/// `shadow-raised` in light theme (dark relies on the surface ladder).
///
/// `isEmergency` adds a 2pt `callstate-emergency` outline; use it only for emergency-calling information.
struct TalkdeskCard<HeaderTrailing: View, Content: View>: View {
    var title: LocalizedStringKey?
    var headerIcon: Image?
    var isEmergency: Bool
    @ViewBuilder var headerTrailing: () -> HeaderTrailing
    @ViewBuilder var content: () -> Content

    init(
        _ title: LocalizedStringKey? = nil,
        headerIcon: Image? = nil,
        isEmergency: Bool = false,
        @ViewBuilder headerTrailing: @escaping () -> HeaderTrailing = { EmptyView() },
        @ViewBuilder content: @escaping () -> Content
    ) {
        self.title = title
        self.headerIcon = headerIcon
        self.isEmergency = isEmergency
        self.headerTrailing = headerTrailing
        self.content = content
    }

    var body: some View {
        let shape = RoundedRectangle(cornerRadius: TalkdeskRadius.md)
        VStack(alignment: .leading, spacing: TalkdeskSpacing.s12) {
            if title != nil || headerIcon != nil {
                HStack(spacing: TalkdeskSpacing.s8) {
                    if let headerIcon {
                        headerIcon
                            .resizable()
                            .frame(width: TalkdeskSizes.icon, height: TalkdeskSizes.icon)
                            .foregroundStyle(isEmergency ? TalkdeskColors.callstateEmergency : TalkdeskColors.iconDefault)
                    }
                    if let title {
                        Text(title)
                            .talkdeskText(TalkdeskTypography.h4)
                            .foregroundStyle(TalkdeskColors.textDefault)
                    }
                    Spacer(minLength: 0)
                    headerTrailing()
                }
            }
            content()
        }
        .padding(TalkdeskSpacing.s16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(TalkdeskColors.surface1, in: shape)
        .overlay {
            shape.strokeBorder(
                isEmergency ? TalkdeskColors.callstateEmergency : TalkdeskColors.borderDivider,
                lineWidth: isEmergency ? 2 : 1
            )
        }
        .talkdeskShadow(.raised)
    }
}

/// Key/value row inside a `TalkdeskCard` (`ta-cell`): key in `caption`/`text-description`, value in
/// `body`. Every cell but the last (`showsDivider: false`) is followed by a `border-divider` line.
struct TalkdeskCardCell: View {
    let key: LocalizedStringKey
    let value: String
    var showsDivider: Bool = true

    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(key)
                .talkdeskText(TalkdeskTypography.caption)
                .foregroundStyle(TalkdeskColors.textDescription)
            Text(value)
                .talkdeskText(TalkdeskTypography.body)
                .foregroundStyle(TalkdeskColors.textDefault)
            if showsDivider {
                Rectangle()
                    .fill(TalkdeskColors.borderDivider)
                    .frame(height: TalkdeskSizes.divider)
                    .padding(.top, TalkdeskSpacing.s12 - 2)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .accessibilityElement(children: .combine)
    }
}

#Preview("Cards") {
    VStack(spacing: 16) {
        TalkdeskCard("Contact details") {
            TalkdeskCardCell(key: "Phone", value: "+1 555 0100")
            TalkdeskCardCell(key: "Company", value: "Acme Inc.", showsDivider: false)
            TalkdeskLink("Show more") {}
        }
        TalkdeskCard("Emergency address", headerIcon: TalkdeskIcons.emergency, isEmergency: true) {
            TalkdeskCardCell(key: "Address", value: "1 Example Street", showsDivider: false)
        }
    }
    .padding()
    .talkdeskTheme()
}
