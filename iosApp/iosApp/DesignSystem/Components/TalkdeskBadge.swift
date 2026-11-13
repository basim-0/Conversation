import SwiftUI

/// Unread count (`ta-badge`): `badge-unread` pill, at least 20pt wide and 20pt tall, `badge` text in
/// `text-on-light-fill`. Counts above 99 show "99+".
struct TalkdeskBadge: View {
    let count: Int

    var body: some View {
        Text(count > 99 ? "99+" : "\(count)")
            .talkdeskText(TalkdeskTypography.badge)
            .foregroundStyle(TalkdeskColors.textOnLightFill)
            .padding(.horizontal, 6)
            .frame(minWidth: 20, minHeight: 20)
            .background(TalkdeskColors.badgeUnread, in: Capsule())
            .accessibilityLabel("\(count) unread")
    }
}

#Preview("Badges") {
    HStack(spacing: 12) {
        TalkdeskBadge(count: 3)
        TalkdeskBadge(count: 42)
        TalkdeskBadge(count: 120)
    }
    .padding()
    .talkdeskTheme()
}
