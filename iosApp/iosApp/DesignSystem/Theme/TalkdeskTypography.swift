import SwiftUI
import UIKit

/// One style of the Talkdesk type scale. Sizes match Android (sp ≙ pt) and scale with Dynamic Type.
///
/// The design system specifies Roboto, which is not bundled; iOS uses the system font at the same metrics.
struct TalkdeskTextStyle: Sendable {
    let size: CGFloat
    let lineHeight: CGFloat
    let weight: Font.Weight
    let tracking: CGFloat
    let relativeTo: UIFont.TextStyle
    var monospacedDigits: Bool = false
}

/// Talkdesk type scale. Apply with `.talkdeskText(TalkdeskTypography.body)`.
enum TalkdeskTypography {
    /// 28/34 Medium. Main screen title (Contacts, Activity).
    static let h1 = TalkdeskTextStyle(size: 28, lineHeight: 34, weight: .medium, tracking: 0, relativeTo: .title1)
    /// 24/30 Regular. Section hero titles, empty-state headline.
    static let h2 = TalkdeskTextStyle(size: 24, lineHeight: 30, weight: .regular, tracking: 0, relativeTo: .title2)
    /// 20/26 Regular. Dialog titles.
    static let h3 = TalkdeskTextStyle(size: 20, lineHeight: 26, weight: .regular, tracking: 0, relativeTo: .title3)
    /// 20/26 Medium. Contact name on call and profile screens.
    static let title = TalkdeskTextStyle(size: 20, lineHeight: 26, weight: .medium, tracking: 0, relativeTo: .title3)
    /// 18/24 Medium. Toolbar title, call contact number, sheet title.
    static let h4 = TalkdeskTextStyle(size: 18, lineHeight: 24, weight: .medium, tracking: 0, relativeTo: .headline)
    /// 15/21 Medium. Buttons, list primary line, emphasised body.
    static let bodyStrong = TalkdeskTextStyle(size: 15, lineHeight: 21, weight: .medium, tracking: 0.1, relativeTo: .body)
    /// 15/21 Regular. Paragraphs, input text, message bubbles.
    static let body = TalkdeskTextStyle(size: 15, lineHeight: 21, weight: .regular, tracking: 0, relativeTo: .body)
    /// 14/20 Medium. Field labels, tab labels, selected chip label.
    static let label = TalkdeskTextStyle(size: 14, lineHeight: 20, weight: .medium, tracking: 0.1, relativeTo: .subheadline)
    /// 14/20 Regular. Chip label, secondary row text.
    static let labelRegular = TalkdeskTextStyle(size: 14, lineHeight: 20, weight: .regular, tracking: 0, relativeTo: .subheadline)
    /// 12/16 Regular. Timestamps, helper text.
    static let caption = TalkdeskTextStyle(size: 12, lineHeight: 16, weight: .regular, tracking: 0.2, relativeTo: .caption1)
    /// 12/16 Medium. Section overline, uppercase, in textBrand.
    static let overline = TalkdeskTextStyle(size: 12, lineHeight: 16, weight: .medium, tracking: 0.6, relativeTo: .caption1)
    /// 11/14 Medium. Bottom navigation label.
    static let navLabel = TalkdeskTextStyle(size: 11, lineHeight: 14, weight: .medium, tracking: 0.3, relativeTo: .caption2)
    /// 24/30 Regular. Dialed number, dial-pad digits.
    static let numberDisplay = TalkdeskTextStyle(size: 24, lineHeight: 30, weight: .regular, tracking: 0.5, relativeTo: .title2)
    /// 18/24 Medium. Call timer (tabular figures).
    static let timer = TalkdeskTextStyle(size: 18, lineHeight: 24, weight: .medium, tracking: 0.5, relativeTo: .headline, monospacedDigits: true)
}

private struct TalkdeskTextModifier: ViewModifier {
    let style: TalkdeskTextStyle
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    func body(content: Content) -> some View {
        let traits = UITraitCollection(preferredContentSizeCategory: UIContentSizeCategory(dynamicTypeSize))
        let metrics = UIFontMetrics(forTextStyle: style.relativeTo)
        let size = metrics.scaledValue(for: style.size, compatibleWith: traits)
        let lineHeight = metrics.scaledValue(for: style.lineHeight, compatibleWith: traits)
        let uiFont = UIFont.systemFont(ofSize: size, weight: style.weight.uiFontWeight)
        let extra = max(0, lineHeight - uiFont.lineHeight)
        var font = Font.system(size: size, weight: style.weight)
        if style.monospacedDigits { font = font.monospacedDigit() }
        return content
            .font(font)
            .tracking(style.tracking)
            .lineSpacing(extra)
            .padding(.vertical, extra / 2)
    }
}

extension View {
    /// Applies a Talkdesk text style (font, tracking and line height).
    func talkdeskText(_ style: TalkdeskTextStyle) -> some View {
        modifier(TalkdeskTextModifier(style: style))
    }
}

extension Font.Weight {
    var uiFontWeight: UIFont.Weight {
        switch self {
        case .medium: .medium
        case .semibold: .semibold
        case .bold: .bold
        case .light: .light
        default: .regular
        }
    }
}
