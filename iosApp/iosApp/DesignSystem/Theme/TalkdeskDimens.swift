import SwiftUI

/// 8pt grid with 4pt half-steps. `s16` is the default screen and card padding.
enum TalkdeskSpacing {
    static let s2: CGFloat = 2
    static let s4: CGFloat = 4
    static let s8: CGFloat = 8
    static let s12: CGFloat = 12
    static let s16: CGFloat = 16
    static let s20: CGFloat = 20
    static let s24: CGFloat = 24
    static let s32: CGFloat = 32
    static let s40: CGFloat = 40
    static let s48: CGFloat = 48
    static let s56: CGFloat = 56
    static let s80: CGFloat = 80
}

/// Corner radii.
enum TalkdeskRadius {
    /// 2. Checkboxes.
    static let xs: CGFloat = 2
    /// 4. Buttons, inputs, toasts.
    static let sm: CGFloat = 4
    /// 8. Cards, dialogs, sheets.
    static let md: CGFloat = 8
    /// 16. Chips.
    static let lg: CGFloat = 16
    /// 24.
    static let xl: CGFloat = 24
    /// Avatars, call buttons, pills (use `Capsule()`/`Circle()`).
    static let full: CGFloat = 9999
}

/// Component and touch-target sizes.
enum TalkdeskSizes {
    static let touch: CGFloat = 48
    static let callButton: CGFloat = 64
    static let control: CGFloat = 56
    static let avatar: CGFloat = 40
    static let avatarLarge: CGFloat = 96
    static let icon: CGFloat = 24
    static let iconSmall: CGFloat = 20
    static let toolbar: CGFloat = 56
    static let bottomNav: CGFloat = 64
    static let chip: CGFloat = 32
    static let input: CGFloat = 48
    static let row: CGFloat = 64
    static let dialpadKey: CGFloat = 64
    static let dragPill: CGFloat = 40
    static let switchTrack: CGFloat = 48
    static let divider: CGFloat = 1
    static let focusRing: CGFloat = 2
    /// Width breakpoint at which tablet layouts start.
    static let breakpointTablet: CGFloat = 600
    static let breakpointTabletLarge: CGFloat = 700
}
