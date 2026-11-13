import ConversationKit
import SwiftUI

/// One Activity row on the DS list row: call-direction or channel icon, spelled-out sub-line, time with unread
/// count, and a call-back button on call rows (hidden while offline). Call rows also swipe left to Call back.
struct ActivityRowView: View {
    let item: ActivityItem
    let offline: Bool
    let onOpen: () -> Void
    let onCallBack: () -> Void

    var body: some View {
        let colors = item.kind.iconColors
        let unread = Int(item.unreadCount)
        let canCallBack = item.kind.isCall && !offline
        TalkdeskListRow(
            icon: item.kind.icon,
            iconTint: colors.tint,
            iconBackground: colors.background,
            title: item.displayTitle,
            isEmphasised: unread > 0,
            subtitle: item.subline,
            time: item.timeLabel,
            badgeCount: unread > 0 ? unread : nil,
            accessibilityDescription: item.spokenDescription,
            onTap: onOpen
        ) {
            if canCallBack {
                TalkdeskRowIconButton(
                    icon: TalkdeskIcons.phone,
                    label: String(localized: "Call back \(item.displayTitle)"),
                    action: onCallBack
                )
            } else if !offline {
                // Keeps message rows aligned with call rows.
                Color.clear.frame(width: 44, height: 44).accessibilityHidden(true)
            }
        }
        .swipeActions(edge: .trailing, allowsFullSwipe: true) {
            if canCallBack {
                Button(action: onCallBack) {
                    Label {
                        Text("Call back")
                    } icon: {
                        TalkdeskIcons.phone
                    }
                }
                .tint(TalkdeskColors.actionCall)
            }
        }
    }
}
