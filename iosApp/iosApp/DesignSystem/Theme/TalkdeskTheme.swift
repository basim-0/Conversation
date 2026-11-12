import SwiftUI

/// The single app theme. Apply `.talkdeskTheme()` once at the root (see `iOSApp`), then read tokens
/// through the same names as Android:
///
/// ```swift
/// Text("Hi")
///     .talkdeskText(TalkdeskTheme.typography.body)
///     .foregroundStyle(TalkdeskTheme.colors.textDefault)
///     .padding(TalkdeskTheme.spacing.s16)
/// ```
///
/// Colours are dynamic and follow the system light/dark appearance.
enum TalkdeskTheme {
    static let colors = TalkdeskColors.self
    static let typography = TalkdeskTypography.self
    static let spacing = TalkdeskSpacing.self
    static let radius = TalkdeskRadius.self
    static let sizes = TalkdeskSizes.self
}

private struct TalkdeskThemeModifier: ViewModifier {
    func body(content: Content) -> some View {
        content
            .tint(TalkdeskColors.textBrand)
            .foregroundStyle(TalkdeskColors.textDefault)
            .background(TalkdeskColors.bgWindow.ignoresSafeArea())
    }
}

extension View {
    /// Applies the Talkdesk theme to a whole screen hierarchy. Use once, at the root.
    func talkdeskTheme() -> some View {
        modifier(TalkdeskThemeModifier())
    }
}
