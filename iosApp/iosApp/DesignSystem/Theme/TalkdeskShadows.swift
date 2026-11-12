import SwiftUI

/// Elevation levels. Light theme draws shadows; dark theme relies on the surface ladder instead.
enum TalkdeskElevation {
    /// `shadow-raised`: 0 1 3 rgba(29,33,35,.12); none in dark.
    case raised
    /// `shadow-overlay`: 0 6 16 rgba(29,33,35,.24); dark rgba(0,0,0,.48).
    case overlay
}

private struct TalkdeskShadowModifier: ViewModifier {
    let elevation: TalkdeskElevation
    @Environment(\.colorScheme) private var colorScheme

    func body(content: Content) -> some View {
        let dark = colorScheme == .dark
        switch elevation {
        case .raised:
            // CSS blur radius ≈ 2 × SwiftUI shadow radius.
            content.shadow(color: dark ? .clear : Color(uiColor: UIColor(hex: 0x1D2123, alpha: 0.12)),
                           radius: 1.5, x: 0, y: 1)
        case .overlay:
            content.shadow(color: dark ? Color.black.opacity(0.48) : Color(uiColor: UIColor(hex: 0x1D2123, alpha: 0.24)),
                           radius: 8, x: 0, y: 6)
        }
    }
}

extension View {
    /// Draws the design-system shadow for the current appearance.
    func talkdeskShadow(_ elevation: TalkdeskElevation) -> some View {
        modifier(TalkdeskShadowModifier(elevation: elevation))
    }
}
