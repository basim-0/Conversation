import SwiftUI

/// Two-line list row (`ta-list` row): 40pt icon disc, title and sub-line, a right-aligned meta column (time over an
/// optional unread badge), then an optional trailing control. At least 64pt tall; a 1pt `border-divider` runs
/// from the text edge (68pt) to the trailing edge. The row body is one button (`onTap`); the trailing control is
/// its own element so it can have its own action.
struct TalkdeskListRow<Trailing: View>: View {
    let icon: Image
    let iconTint: Color
    let iconBackground: Color
    let title: String
    /// Bold title, for rows with unread items.
    var isEmphasised: Bool = false
    let subtitle: String
    let time: String
    var badgeCount: Int?
    /// Spoken description of the whole row body.
    var accessibilityDescription: String?
    let onTap: () -> Void
    @ViewBuilder var trailing: () -> Trailing

    var body: some View {
        HStack(spacing: 0) {
            Button(action: onTap) {
                HStack(spacing: TalkdeskSpacing.s12) {
                    TalkdeskListRowIcon(icon: icon, tint: iconTint, background: iconBackground)
                    VStack(alignment: .leading, spacing: 0) {
                        Text(title)
                            .talkdeskText(isEmphasised ? TalkdeskTypography.rowTitleUnread : TalkdeskTypography.rowTitle)
                            .foregroundStyle(TalkdeskColors.textDefault)
                            .lineLimit(1)
                        Text(subtitle)
                            .talkdeskText(TalkdeskTypography.rowSubtitle)
                            .foregroundStyle(TalkdeskColors.textDescription)
                            .lineLimit(1)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    TalkdeskListRowMeta(time: time, badgeCount: badgeCount)
                }
                .padding(.vertical, TalkdeskSpacing.s8)
                .contentShape(Rectangle())
            }
            .buttonStyle(TalkdeskListRowButtonStyle())
            .accessibilityElement(children: .ignore)
            .accessibilityLabel(accessibilityDescription ?? "\(subtitle), \(title), \(time)")
            .accessibilityAddTraits(.isButton)
            trailing()
        }
        .padding(.leading, TalkdeskSpacing.s16)
        .padding(.trailing, Trailing.self == EmptyView.self ? TalkdeskSpacing.s16 : TalkdeskSpacing.s8)
        .frame(minHeight: TalkdeskSizes.row)
        .overlay(alignment: .bottom) {
            Rectangle()
                .fill(TalkdeskColors.borderDivider)
                .frame(height: TalkdeskSizes.divider)
                .padding(.leading, 68)
        }
    }
}

/// 40pt disc with a 20pt icon (`avatar` size) on a subtle fill.
struct TalkdeskListRowIcon: View {
    let icon: Image
    let tint: Color
    let background: Color

    var body: some View {
        icon
            .resizable()
            .frame(width: TalkdeskSizes.iconSmall, height: TalkdeskSizes.iconSmall)
            .foregroundStyle(tint)
            .frame(width: TalkdeskSizes.avatar, height: TalkdeskSizes.avatar)
            .background(background, in: Circle())
            .accessibilityHidden(true)
    }
}

/// Time (13/18, `text-description`) over an optional unread badge, right-aligned.
struct TalkdeskListRowMeta: View {
    let time: String
    var badgeCount: Int?

    var body: some View {
        VStack(alignment: .trailing, spacing: TalkdeskSpacing.s4) {
            Text(time)
                .talkdeskText(TalkdeskTypography.rowMeta)
                .foregroundStyle(TalkdeskColors.textDescription)
                .lineLimit(1)
            if let badgeCount, badgeCount > 0 {
                TalkdeskBadge(count: badgeCount)
            }
        }
        .fixedSize()
    }
}

/// 44pt borderless icon button in `icon-brand`, for the trailing action of a row.
struct TalkdeskRowIconButton: View {
    let icon: Image
    let label: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            icon
                .resizable()
                .frame(width: TalkdeskSizes.iconSmall, height: TalkdeskSizes.iconSmall)
                .foregroundStyle(TalkdeskColors.iconBrand)
                .frame(width: 44, height: 44)
                .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .accessibilityLabel(label)
    }
}

private struct TalkdeskListRowButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .background(configuration.isPressed ? TalkdeskColors.surfaceSelected : Color.clear)
    }
}

#Preview("List rows") {
    VStack(spacing: 0) {
        TalkdeskListRow(
            icon: TalkdeskIcons.phoneMissed,
            iconTint: TalkdeskColors.callMissed,
            iconBackground: TalkdeskColors.fillErrorSubtle,
            title: "+1 555 0187",
            subtitle: "Missed",
            time: "10:42",
            onTap: {}
        ) {
            TalkdeskRowIconButton(icon: TalkdeskIcons.phone, label: "Call back +1 555 0187", action: {})
        }
        TalkdeskListRow(
            icon: TalkdeskIcons.whatsapp,
            iconTint: TalkdeskColors.iconBrand,
            iconBackground: TalkdeskColors.surfaceBrandSubtle,
            title: "Jordan Lee",
            isEmphasised: true,
            subtitle: "WhatsApp · by Ana (Me)",
            time: "08:50",
            badgeCount: 2,
            onTap: {}
        ) {
            Color.clear.frame(width: 44)
        }
    }
    .talkdeskTheme()
}
