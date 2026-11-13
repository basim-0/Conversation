import SwiftUI

/// iOS-native text styles used by the Activity board ("Activity · iOS"). They sit beside the shared scale in
/// `TalkdeskTypography` because the iOS design uses weights and sizes (16/21 row title, 13/18 meta) that the Android scale does not have. Same system font as the rest of iOS.
extension TalkdeskTypography {
    /// 16/21 Medium. List row title.
    static let rowTitle = TalkdeskTextStyle(size: 16, lineHeight: 21, weight: .medium, tracking: 0, relativeTo: .body)
    /// 16/21 Bold. List row title with unread items.
    static let rowTitleUnread = TalkdeskTextStyle(size: 16, lineHeight: 21, weight: .bold, tracking: 0, relativeTo: .body)
    /// 14/19 Regular. List row sub-line.
    static let rowSubtitle = TalkdeskTextStyle(size: 14, lineHeight: 19, weight: .regular, tracking: 0, relativeTo: .subheadline)
    /// 13/18 Regular. List row time.
    static let rowMeta = TalkdeskTextStyle(size: 13, lineHeight: 18, weight: .regular, tracking: 0, relativeTo: .footnote)
    /// 13/18 Semibold. Day header above a group of rows.
    static let sectionHeader = TalkdeskTextStyle(size: 13, lineHeight: 18, weight: .semibold, tracking: 0, relativeTo: .footnote)
    /// 18/24 Semibold. Empty-state title.
    static let emptyTitle = TalkdeskTextStyle(size: 18, lineHeight: 24, weight: .semibold, tracking: 0, relativeTo: .headline)
    /// 15/21 Semibold. Pill button label.
    static let pillButton = TalkdeskTextStyle(size: 15, lineHeight: 21, weight: .semibold, tracking: 0, relativeTo: .body)
    /// 12/16 Medium. Count inside a badge.
    static let badge = TalkdeskTextStyle(size: 12, lineHeight: 16, weight: .medium, tracking: 0, relativeTo: .caption1)
}
