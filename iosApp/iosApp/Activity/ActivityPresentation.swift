import ConversationKit
import SwiftUI

/// Maps the shared Activity models to what the SwiftUI rows show. Same rules as Android's `ActivityRow`:
/// call direction or channel icon, spelled-out sub-line, "Unknown contact" for no title.
extension ActivityKind {
    var icon: Image {
        switch onEnum(of: self) {
        case .call(let call):
            switch call.direction {
            case .inbound: TalkdeskIcons.phoneIn
            case .outbound: TalkdeskIcons.phoneOut
            case .missed, .abandoned, .notAnswered: TalkdeskIcons.phoneMissed
            case .transfer: TalkdeskIcons.transfer
            }
        case .message(let message):
            switch message.channel {
            case .sms: TalkdeskIcons.sms
            case .whatsApp: TalkdeskIcons.whatsapp
            case .email: TalkdeskIcons.mail
            }
        }
    }

    /// Icon colour and disc fill: green inbound, brand outbound, red missed; messages and transfers in brand.
    var iconColors: (tint: Color, background: Color) {
        switch onEnum(of: self) {
        case .call(let call):
            switch call.direction {
            case .inbound: (TalkdeskColors.callInbound, TalkdeskColors.fillSuccessSubtle)
            case .outbound: (TalkdeskColors.callOutbound, TalkdeskColors.surfaceBrandSubtle)
            case .missed, .abandoned, .notAnswered: (TalkdeskColors.callMissed, TalkdeskColors.fillErrorSubtle)
            case .transfer: (TalkdeskColors.iconBrand, TalkdeskColors.surfaceBrandSubtle)
            }
        case .message:
            (TalkdeskColors.iconBrand, TalkdeskColors.surfaceBrandSubtle)
        }
    }

    var label: String {
        switch onEnum(of: self) {
        case .call(let call):
            switch call.direction {
            case .inbound: String(localized: "Inbound")
            case .outbound: String(localized: "Outbound")
            case .missed: String(localized: "Missed")
            case .abandoned: String(localized: "Abandoned")
            case .notAnswered: String(localized: "Not answered")
            case .transfer: String(localized: "Transfer")
            }
        case .message(let message):
            switch message.channel {
            case .sms: String(localized: "SMS")
            case .whatsApp: String(localized: "WhatsApp")
            case .email: String(localized: "Email")
            }
        }
    }

    var isCall: Bool {
        if case .call = onEnum(of: self) { true } else { false }
    }
}

extension ActivityItem {
    var displayTitle: String {
        title ?? String(localized: "Unknown contact")
    }

    /// "Inbound · by Ana (Me) · 4:12"; only the kind label when there is no agent or duration.
    var subline: String {
        var details: [String] = []
        if let agentName {
            details.append(isMine ? String(localized: "by \(agentName) (Me)") : String(localized: "by \(agentName)"))
        }
        if let durationLabel { details.append(durationLabel) }
        let kindLabel = kind.label
        return details.isEmpty ? kindLabel : ([kindLabel] + details).joined(separator: " · ")
    }

    var spokenDescription: String {
        var text = "\(subline), \(displayTitle), \(timeLabel)"
        if unreadCount > 0 { text += ", " + String(localized: "\(Int(unreadCount)) unread") }
        return text
    }
}

extension ActivityDay {
    var headerLabel: String {
        switch onEnum(of: self) {
        case .today: String(localized: "Today")
        case .yesterday: String(localized: "Yesterday")
        case .date(let date): date.label
        }
    }
}

extension ActivityChannel {
    /// Order shown in the filter menu and the "Filtered by" line.
    static let filterOrder: [ActivityChannel] = [.calls, .sms, .whatsApp, .email]

    var title: LocalizedStringKey {
        switch self {
        case .calls: "Calls"
        case .sms: "SMS"
        case .whatsApp: "WhatsApp"
        case .email: "Email"
        }
    }

    var plainTitle: String {
        switch self {
        case .calls: String(localized: "Calls")
        case .sms: String(localized: "SMS")
        case .whatsApp: String(localized: "WhatsApp")
        case .email: String(localized: "Email")
        }
    }

    var icon: Image {
        switch self {
        case .calls: TalkdeskIcons.phone
        case .sms: TalkdeskIcons.sms
        case .whatsApp: TalkdeskIcons.whatsapp
        case .email: TalkdeskIcons.mail
        }
    }
}
