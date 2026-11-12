// Generated from the Talkdesk Agent Mobile design system (tokens.json). Semantic tokens only;
// Cobalt primitives are reference-only and intentionally not exposed. Edit values in the design
// system first, then regenerate.

import SwiftUI

/// Semantic colour tokens of the Talkdesk design system. Each colour resolves light/dark from the trait collection.
enum TalkdeskColors {
    /// Screen background behind everything (Cobalt Backgrounds/default).
    static let bgWindow = Color(light: 0xFFFFFF, dark: 0x1A1A1A)
    /// Grouped-list and conversation backgrounds that hold floating cards (light: Cobalt Backgrounds/secondary; dark: Backgrounds/default so surface-1 cards stand out).
    static let bgSubtle = Color(light: 0xF8F9FA, dark: 0x1A1A1A)
    /// Cards, list containers, toolbar, inputs (Cobalt surface-elevation-1).
    static let surface1 = Color(light: 0xFFFFFF, dark: 0x1D2123)
    /// Bottom sheets, dialogs, tablet modals, menus (Cobalt surface-elevation-2).
    static let surface2 = Color(light: 0xFFFFFF, dark: 0x25292D)
    /// Content raised on a sheet: chips, avatar discs, nested cards (Cobalt surface-elevation-3).
    static let surface3 = Color(light: 0xF8F9FA, dark: 0x2B2F33)
    /// Bottom navigation bar and tablet navigation rail.
    static let surfaceNav = Color(light: 0xF8F9FA, dark: 0x1D2123)
    /// Inbound message bubble, client transcript bubble, disabled input, inset fields.
    static let surfaceSunken = Color(light: 0xEEF1F2, dark: 0x25292D)
    /// Selected list row and active master item on tablet (Cobalt Backgrounds/primary: selected rows).
    static let surfaceSelected = Color(light: 0xF9F8FF, dark: 0x1C1735)
    /// Avatar initials disc, tag chips, agent transcript bubble in light theme.
    static let surfaceBrandSubtle = Color(light: 0xF2F0FF, dark: 0x2B2F33)
    /// Dial pad key face.
    static let surfaceKey = Color(light: 0xFFFFFF, dark: 0x25292D)
    /// Shimmer skeleton block resting colour.
    static let skeletonBase = Color(light: 0xEEF1F2, dark: 0x25292D)
    /// Shimmer skeleton sweep highlight.
    static let skeletonHighlight = Color(light: 0xE6E9EB, dark: 0x2B2F33)
    /// Overlay behind bottom sheets and dialogs.
    static let scrim = Color(light: 0x1D2123, dark: 0x1A1A1A, lightAlpha: 0.5, darkAlpha: 0.72)
    /// Body copy, titles, list primary lines on every bg-* and surface-* token (16:1 light, 10:1+ dark).
    static let textDefault = Color(light: 0x1D2123, dark: 0xDEE2E5)
    /// Secondary lines, timestamps, helper text on bg-* and surface-* (5.2:1+ light, 6.7:1+ dark).
    static let textDescription = Color(light: 0x606569, dark: 0xB2B7BC)
    /// Input and search placeholders on surface-1 and surface-sunken (5.2:1+ light, 7.2:1+ dark). Same value as text-description; kept separate to map to Cobalt --text-placeholder.
    static let textPlaceholder = Color(light: 0x606569, dark: 0xB2B7BC)
    /// Disabled labels. Exempt from contrast minimums; always pair with a disabled affordance.
    static let textDisabled = Color(light: 0xB6BCC0, dark: 0x606569)
    /// Links, active tab label, overline, brand-coloured numbers on any surface (6.0:1+ light, 6.8:1+ dark; Cobalt Primary/11).
    static let textBrand = Color(light: 0x6A41D2, dark: 0xB5A5FF)
    /// Labels and icons on action-primary and bubble-outbound (9.8:1 both themes; Cobalt Primary/contrast).
    static let textOnBrand = Color(light: 0xFFFFFF, dark: 0xFFFFFF)
    /// Labels on fill-success, fill-error, action-call, action-end (4.6:1+).
    static let textOnFill = Color(light: 0xFFFFFF, dark: 0xFFFFFF)
    /// Labels on fill-warning, badge-unread, highlight-* and any light fill (5.4:1+).
    static let textOnLightFill = Color(light: 0x1D2123, dark: 0x1D2123)
    /// Label on action-primary-disabled (disabled state; exempt from contrast minimums).
    static let textOnDisabled = Color(light: 0xFFFFFF, dark: 0x7D8287)
    /// Error messages, destructive borderless buttons, mandatory asterisk on surfaces (5.5:1 light, 6.1:1+ dark).
    static let textError = Color(light: 0xCC2018, dark: 0xEE8D7F)
    /// Success messages and 'Connected' labels on surfaces (5.5:1 light, 6.7:1+ dark).
    static let textSuccess = Color(light: 0x007942, dark: 0x61B885)
    /// Warning messages and 'Away' labels on surfaces (4.6:1 light, 11:1 dark).
    static let textWarning = Color(light: 0xA26A00, dark: 0xFFCC61)
    /// Info messages, 'Dialing…' label, borderless info button on surfaces (6.0:1 light, 11:1 dark).
    static let textInfo = Color(light: 0x275DCC, dark: 0xB8D3FF)
    /// Input, outlined-button, checkbox and radio outlines (3.9:1 light on surface-1, 3.8:1+ dark).
    static let borderControl = Color(light: 0x7D8287, dark: 0x7D8287)
    /// Hairline dividers between rows and sections. Decorative only.
    static let borderDivider = Color(light: 0xE6E9EB, dark: 0x2B2F33)
    /// 2dp focus ring on every focusable control (6.0:1 light, 6.8:1+ dark).
    static let borderFocus = Color(light: 0x6A41D2, dark: 0xB5A5FF)
    /// Input outline in the error state.
    static let borderError = Color(light: 0xCC2018, dark: 0xEE8D7F)
    /// Selected filter chip and selected card outline.
    static let borderSelected = Color(light: 0x5405BD, dark: 0xB5A5FF)
    /// Toolbar and list icons.
    static let iconDefault = Color(light: 0x1D2123, dark: 0xDEE2E5)
    /// Secondary/trailing icons, chevrons, inactive bottom-nav icons.
    static let iconMuted = Color(light: 0x606569, dark: 0xB2B7BC)
    /// Active bottom-nav icon, brand action icons.
    static let iconBrand = Color(light: 0x6A41D2, dark: 0xB5A5FF)
    /// Filled primary button, send button, active toggle fill (Cobalt Primary/9; same in both themes).
    static let actionPrimary = Color(light: 0x5405BD, dark: 0x5405BD)
    /// Pressed/hover state of action-primary (Cobalt Primary/10).
    static let actionPrimaryPressed = Color(light: 0x4600A6, dark: 0x44009F)
    /// Disabled primary fill; label uses text-on-brand in light, text-disabled in dark.
    static let actionPrimaryDisabled = Color(light: 0xD1C8FF, dark: 0x2B2F33)
    /// Accept-call and start-call buttons; white icon (4.6:1).
    static let actionCall = Color(light: 0x00874F, dark: 0x00874F)
    /// Pressed state of action-call.
    static let actionCallPressed = Color(light: 0x007942, dark: 0x007942)
    /// End-call button; white icon (4.6:1).
    static let actionEnd = Color(light: 0xDB362A, dark: 0xDB362A)
    /// Reject incoming call and destructive dialog confirm (5.5:1 with white).
    static let actionReject = Color(light: 0xCC2018, dark: 0xCC2018)
    /// Checked checkbox/radio, switch-on track, selected segment.
    static let controlSelected = Color(light: 0x5405BD, dark: 0xB5A5FF)
    /// Checkmark/thumb drawn on control-selected (9.8:1 light, 7.6:1 dark).
    static let controlOnSelected = Color(light: 0xFFFFFF, dark: 0x1D2123)
    /// Success icons and the healthy end of meters.
    static let statusSuccess = Color(light: 0x00874F, dark: 0x61B885)
    /// Warning icons.
    static let statusWarning = Color(light: 0xA26A00, dark: 0xFFCC61)
    /// Error icons, missed-call glyph.
    static let statusError = Color(light: 0xDB362A, dark: 0xEE8D7F)
    /// Info icons.
    static let statusInfo = Color(light: 0x275DCC, dark: 0xB8D3FF)
    /// Success toast background.
    static let fillSuccess = Color(light: 0x00874F, dark: 0x00874F)
    /// Error toast and threshold banner background.
    static let fillError = Color(light: 0xCC2018, dark: 0xCC2018)
    /// Warning toast background (text-on-light-fill).
    static let fillWarning = Color(light: 0xFFCC61, dark: 0xFFCC61)
    /// Info toast background.
    static let fillInfo = Color(light: 0x003FBD, dark: 0x003FBD)
    /// Wrap-up auto-dismiss banner; text in text-error.
    static let fillErrorSubtle = Color(light: 0xFFEBE8, dark: 0x25292D)
    /// Success inline message background; text in text-success.
    static let fillSuccessSubtle = Color(light: 0xE6F6EB, dark: 0x25292D)
    /// Warning inline message background; text in text-warning.
    static let fillWarningSubtle = Color(light: 0xFFF9E7, dark: 0x25292D)
    /// Info inline message background; text in text-info.
    static let fillInfoSubtle = Color(light: 0xECF2FF, dark: 0x25292D)
    /// Available presence dot/ring; always paired with the word 'Available'.
    static let presenceAvailable = Color(light: 0x00874F, dark: 0x61B885)
    /// Busy / On a call presence; paired with label.
    static let presenceBusy = Color(light: 0xDB362A, dark: 0xEE8D7F)
    /// Away presence; paired with label.
    static let presenceAway = Color(light: 0xA26A00, dark: 0xFFCC61)
    /// Offline presence; paired with label.
    static let presenceOffline = Color(light: 0x7D8287, dark: 0x7D8287)
    /// Inbound direction icon in activity rows.
    static let callInbound = Color(light: 0x00874F, dark: 0x61B885)
    /// Outbound direction icon.
    static let callOutbound = Color(light: 0x6A41D2, dark: 0xB5A5FF)
    /// Missed and abandoned direction icons; label text stays text-default.
    static let callMissed = Color(light: 0xDB362A, dark: 0xEE8D7F)
    /// Incoming pulse rings (drawn at 40/24/12% opacity).
    static let callstateIncoming = Color(light: 0x5405BD, dark: 0xB5A5FF)
    /// Dialing pulse rings and 'Dialing…' header (Cobalt Conversations: blue 11).
    static let callstateDialing = Color(light: 0x275DCC, dark: 0xB8D3FF)
    /// On-call rings, talking icon and header (Cobalt Conversations: green 11).
    static let callstateOncall = Color(light: 0x00874F, dark: 0x61B885)
    /// Emergency rings and banner icon.
    static let callstateEmergency = Color(light: 0xDB362A, dark: 0xEE8D7F)
    /// Wrap-up header and timer (Cobalt Conversations: red 11).
    static let callstateWrapup = Color(light: 0xCC2018, dark: 0xEE8D7F)
    /// Call quality: 3 bars filled.
    static let qualityExcellent = Color(light: 0x00874F, dark: 0x61B885)
    /// Call quality: 2 bars filled.
    static let qualityGood = Color(light: 0xA26A00, dark: 0xFFCC61)
    /// Call quality: 1 bar / no internet (4.4:1 light, 5.8:1 dark).
    static let qualityBad = Color(light: 0xD94300, dark: 0xFF6300)
    /// Unread counter fill; count in text-on-light-fill.
    static let badgeUnread = Color(light: 0xFF6300, dark: 0xFF6300)
    /// Outbound SMS/WhatsApp bubble; text-on-brand.
    static let bubbleOutbound = Color(light: 0x5405BD, dark: 0x5405BD)
    /// Inbound bubble; text-default.
    static let bubbleInbound = Color(light: 0xEEF1F2, dark: 0x25292D)
    /// Agent transcript bubble fill.
    static let transcriptAgent = Color(light: 0xF2F0FF, dark: 0x1C1735)
    /// Agent transcript bubble outline.
    static let transcriptAgentBorder = Color(light: 0xD1C8FF, dark: 0x44009F)
    /// Client transcript bubble fill.
    static let transcriptClient = Color(light: 0xEEF1F2, dark: 0x25292D)
    /// 'Live' label and dot in transcription header (8.6:1 light).
    static let transcriptLive = Color(light: 0x003FBD, dark: 0xB8D3FF)
    /// Search match highlight in transcript; text-on-light-fill.
    static let highlightMatch = Color(light: 0xFFF1C4, dark: 0xFFDC8E)
    /// Current (focused) search match; text-on-light-fill.
    static let highlightCurrent = Color(light: 0xFFCC61, dark: 0xEDA813)
    /// Progress, slider and voicemail track.
    static let track = Color(light: 0xE6E9EB, dark: 0x2B2F33)
    /// Progress and voicemail played portion (meets 3:1 on track).
    static let trackFill = Color(light: 0x6A41D2, dark: 0xB5A5FF)
}

extension Color {
    /// A colour that follows the system appearance.
    init(light: UInt32, dark: UInt32, lightAlpha: Double = 1, darkAlpha: Double = 1) {
        self.init(uiColor: UIColor { traits in
            traits.userInterfaceStyle == .dark
                ? UIColor(hex: dark, alpha: darkAlpha)
                : UIColor(hex: light, alpha: lightAlpha)
        })
    }
}

extension UIColor {
    convenience init(hex: UInt32, alpha: Double = 1) {
        self.init(
            red: CGFloat((hex >> 16) & 0xFF) / 255,
            green: CGFloat((hex >> 8) & 0xFF) / 255,
            blue: CGFloat(hex & 0xFF) / 255,
            alpha: CGFloat(alpha)
        )
    }
}
