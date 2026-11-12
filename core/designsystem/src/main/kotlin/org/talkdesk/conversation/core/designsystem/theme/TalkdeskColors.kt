// Generated from the Talkdesk Agent Mobile design system (tokens.json). Semantic tokens only;
// Cobalt primitives are reference-only and intentionally not exposed. Edit values in the design
// system first, then regenerate.

package org.talkdesk.conversation.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Semantic colour tokens of the Talkdesk design system. Read them through [TalkdeskTheme.colors]. */
@Immutable
data class TalkdeskColors(
    /** Screen background behind everything (Cobalt Backgrounds/default). */
    val bgWindow: Color,
    /** Grouped-list and conversation backgrounds that hold floating cards (light: Cobalt Backgrounds/secondary; dark: Backgrounds/default so surface-1 cards stand out). */
    val bgSubtle: Color,
    /** Cards, list containers, toolbar, inputs (Cobalt surface-elevation-1). */
    val surface1: Color,
    /** Bottom sheets, dialogs, tablet modals, menus (Cobalt surface-elevation-2). */
    val surface2: Color,
    /** Content raised on a sheet: chips, avatar discs, nested cards (Cobalt surface-elevation-3). */
    val surface3: Color,
    /** Bottom navigation bar and tablet navigation rail. */
    val surfaceNav: Color,
    /** Inbound message bubble, client transcript bubble, disabled input, inset fields. */
    val surfaceSunken: Color,
    /** Selected list row and active master item on tablet (Cobalt Backgrounds/primary: selected rows). */
    val surfaceSelected: Color,
    /** Avatar initials disc, tag chips, agent transcript bubble in light theme. */
    val surfaceBrandSubtle: Color,
    /** Dial pad key face. */
    val surfaceKey: Color,
    /** Shimmer skeleton block resting colour. */
    val skeletonBase: Color,
    /** Shimmer skeleton sweep highlight. */
    val skeletonHighlight: Color,
    /** Overlay behind bottom sheets and dialogs. */
    val scrim: Color,
    /** Body copy, titles, list primary lines on every bg-* and surface-* token (16:1 light, 10:1+ dark). */
    val textDefault: Color,
    /** Secondary lines, timestamps, helper text on bg-* and surface-* (5.2:1+ light, 6.7:1+ dark). */
    val textDescription: Color,
    /** Input and search placeholders on surface-1 and surface-sunken (5.2:1+ light, 7.2:1+ dark). Same value as text-description; kept separate to map to Cobalt --text-placeholder. */
    val textPlaceholder: Color,
    /** Disabled labels. Exempt from contrast minimums; always pair with a disabled affordance. */
    val textDisabled: Color,
    /** Links, active tab label, overline, brand-coloured numbers on any surface (6.0:1+ light, 6.8:1+ dark; Cobalt Primary/11). */
    val textBrand: Color,
    /** Labels and icons on action-primary and bubble-outbound (9.8:1 both themes; Cobalt Primary/contrast). */
    val textOnBrand: Color,
    /** Labels on fill-success, fill-error, action-call, action-end (4.6:1+). */
    val textOnFill: Color,
    /** Labels on fill-warning, badge-unread, highlight-* and any light fill (5.4:1+). */
    val textOnLightFill: Color,
    /** Label on action-primary-disabled (disabled state; exempt from contrast minimums). */
    val textOnDisabled: Color,
    /** Error messages, destructive borderless buttons, mandatory asterisk on surfaces (5.5:1 light, 6.1:1+ dark). */
    val textError: Color,
    /** Success messages and 'Connected' labels on surfaces (5.5:1 light, 6.7:1+ dark). */
    val textSuccess: Color,
    /** Warning messages and 'Away' labels on surfaces (4.6:1 light, 11:1 dark). */
    val textWarning: Color,
    /** Info messages, 'Dialing…' label, borderless info button on surfaces (6.0:1 light, 11:1 dark). */
    val textInfo: Color,
    /** Input, outlined-button, checkbox and radio outlines (3.9:1 light on surface-1, 3.8:1+ dark). */
    val borderControl: Color,
    /** Hairline dividers between rows and sections. Decorative only. */
    val borderDivider: Color,
    /** 2dp focus ring on every focusable control (6.0:1 light, 6.8:1+ dark). */
    val borderFocus: Color,
    /** Input outline in the error state. */
    val borderError: Color,
    /** Selected filter chip and selected card outline. */
    val borderSelected: Color,
    /** Toolbar and list icons. */
    val iconDefault: Color,
    /** Secondary/trailing icons, chevrons, inactive bottom-nav icons. */
    val iconMuted: Color,
    /** Active bottom-nav icon, brand action icons. */
    val iconBrand: Color,
    /** Filled primary button, send button, active toggle fill (Cobalt Primary/9; same in both themes). */
    val actionPrimary: Color,
    /** Pressed/hover state of action-primary (Cobalt Primary/10). */
    val actionPrimaryPressed: Color,
    /** Disabled primary fill; label uses text-on-brand in light, text-disabled in dark. */
    val actionPrimaryDisabled: Color,
    /** Accept-call and start-call buttons; white icon (4.6:1). */
    val actionCall: Color,
    /** Pressed state of action-call. */
    val actionCallPressed: Color,
    /** End-call button; white icon (4.6:1). */
    val actionEnd: Color,
    /** Reject incoming call and destructive dialog confirm (5.5:1 with white). */
    val actionReject: Color,
    /** Checked checkbox/radio, switch-on track, selected segment. */
    val controlSelected: Color,
    /** Checkmark/thumb drawn on control-selected (9.8:1 light, 7.6:1 dark). */
    val controlOnSelected: Color,
    /** Success icons and the healthy end of meters. */
    val statusSuccess: Color,
    /** Warning icons. */
    val statusWarning: Color,
    /** Error icons, missed-call glyph. */
    val statusError: Color,
    /** Info icons. */
    val statusInfo: Color,
    /** Success toast background. */
    val fillSuccess: Color,
    /** Error toast and threshold banner background. */
    val fillError: Color,
    /** Warning toast background (text-on-light-fill). */
    val fillWarning: Color,
    /** Info toast background. */
    val fillInfo: Color,
    /** Wrap-up auto-dismiss banner; text in text-error. */
    val fillErrorSubtle: Color,
    /** Success inline message background; text in text-success. */
    val fillSuccessSubtle: Color,
    /** Warning inline message background; text in text-warning. */
    val fillWarningSubtle: Color,
    /** Info inline message background; text in text-info. */
    val fillInfoSubtle: Color,
    /** Available presence dot/ring; always paired with the word 'Available'. */
    val presenceAvailable: Color,
    /** Busy / On a call presence; paired with label. */
    val presenceBusy: Color,
    /** Away presence; paired with label. */
    val presenceAway: Color,
    /** Offline presence; paired with label. */
    val presenceOffline: Color,
    /** Inbound direction icon in activity rows. */
    val callInbound: Color,
    /** Outbound direction icon. */
    val callOutbound: Color,
    /** Missed and abandoned direction icons; label text stays text-default. */
    val callMissed: Color,
    /** Incoming pulse rings (drawn at 40/24/12% opacity). */
    val callstateIncoming: Color,
    /** Dialing pulse rings and 'Dialing…' header (Cobalt Conversations: blue 11). */
    val callstateDialing: Color,
    /** On-call rings, talking icon and header (Cobalt Conversations: green 11). */
    val callstateOncall: Color,
    /** Emergency rings and banner icon. */
    val callstateEmergency: Color,
    /** Wrap-up header and timer (Cobalt Conversations: red 11). */
    val callstateWrapup: Color,
    /** Call quality: 3 bars filled. */
    val qualityExcellent: Color,
    /** Call quality: 2 bars filled. */
    val qualityGood: Color,
    /** Call quality: 1 bar / no internet (4.4:1 light, 5.8:1 dark). */
    val qualityBad: Color,
    /** Unread counter fill; count in text-on-light-fill. */
    val badgeUnread: Color,
    /** Outbound SMS/WhatsApp bubble; text-on-brand. */
    val bubbleOutbound: Color,
    /** Inbound bubble; text-default. */
    val bubbleInbound: Color,
    /** Agent transcript bubble fill. */
    val transcriptAgent: Color,
    /** Agent transcript bubble outline. */
    val transcriptAgentBorder: Color,
    /** Client transcript bubble fill. */
    val transcriptClient: Color,
    /** 'Live' label and dot in transcription header (8.6:1 light). */
    val transcriptLive: Color,
    /** Search match highlight in transcript; text-on-light-fill. */
    val highlightMatch: Color,
    /** Current (focused) search match; text-on-light-fill. */
    val highlightCurrent: Color,
    /** Progress, slider and voicemail track. */
    val track: Color,
    /** Progress and voicemail played portion (meets 3:1 on track). */
    val trackFill: Color,
    val isDark: Boolean,
)

internal val LightTalkdeskColors = TalkdeskColors(
    bgWindow = Color(0xFFFFFFFF),
    bgSubtle = Color(0xFFF8F9FA),
    surface1 = Color(0xFFFFFFFF),
    surface2 = Color(0xFFFFFFFF),
    surface3 = Color(0xFFF8F9FA),
    surfaceNav = Color(0xFFF8F9FA),
    surfaceSunken = Color(0xFFEEF1F2),
    surfaceSelected = Color(0xFFF9F8FF),
    surfaceBrandSubtle = Color(0xFFF2F0FF),
    surfaceKey = Color(0xFFFFFFFF),
    skeletonBase = Color(0xFFEEF1F2),
    skeletonHighlight = Color(0xFFE6E9EB),
    scrim = Color(0xFF1D2123).copy(alpha = 0.5f),
    textDefault = Color(0xFF1D2123),
    textDescription = Color(0xFF606569),
    textPlaceholder = Color(0xFF606569),
    textDisabled = Color(0xFFB6BCC0),
    textBrand = Color(0xFF6A41D2),
    textOnBrand = Color(0xFFFFFFFF),
    textOnFill = Color(0xFFFFFFFF),
    textOnLightFill = Color(0xFF1D2123),
    textOnDisabled = Color(0xFFFFFFFF),
    textError = Color(0xFFCC2018),
    textSuccess = Color(0xFF007942),
    textWarning = Color(0xFFA26A00),
    textInfo = Color(0xFF275DCC),
    borderControl = Color(0xFF7D8287),
    borderDivider = Color(0xFFE6E9EB),
    borderFocus = Color(0xFF6A41D2),
    borderError = Color(0xFFCC2018),
    borderSelected = Color(0xFF5405BD),
    iconDefault = Color(0xFF1D2123),
    iconMuted = Color(0xFF606569),
    iconBrand = Color(0xFF6A41D2),
    actionPrimary = Color(0xFF5405BD),
    actionPrimaryPressed = Color(0xFF4600A6),
    actionPrimaryDisabled = Color(0xFFD1C8FF),
    actionCall = Color(0xFF00874F),
    actionCallPressed = Color(0xFF007942),
    actionEnd = Color(0xFFDB362A),
    actionReject = Color(0xFFCC2018),
    controlSelected = Color(0xFF5405BD),
    controlOnSelected = Color(0xFFFFFFFF),
    statusSuccess = Color(0xFF00874F),
    statusWarning = Color(0xFFA26A00),
    statusError = Color(0xFFDB362A),
    statusInfo = Color(0xFF275DCC),
    fillSuccess = Color(0xFF00874F),
    fillError = Color(0xFFCC2018),
    fillWarning = Color(0xFFFFCC61),
    fillInfo = Color(0xFF003FBD),
    fillErrorSubtle = Color(0xFFFFEBE8),
    fillSuccessSubtle = Color(0xFFE6F6EB),
    fillWarningSubtle = Color(0xFFFFF9E7),
    fillInfoSubtle = Color(0xFFECF2FF),
    presenceAvailable = Color(0xFF00874F),
    presenceBusy = Color(0xFFDB362A),
    presenceAway = Color(0xFFA26A00),
    presenceOffline = Color(0xFF7D8287),
    callInbound = Color(0xFF00874F),
    callOutbound = Color(0xFF6A41D2),
    callMissed = Color(0xFFDB362A),
    callstateIncoming = Color(0xFF5405BD),
    callstateDialing = Color(0xFF275DCC),
    callstateOncall = Color(0xFF00874F),
    callstateEmergency = Color(0xFFDB362A),
    callstateWrapup = Color(0xFFCC2018),
    qualityExcellent = Color(0xFF00874F),
    qualityGood = Color(0xFFA26A00),
    qualityBad = Color(0xFFD94300),
    badgeUnread = Color(0xFFFF6300),
    bubbleOutbound = Color(0xFF5405BD),
    bubbleInbound = Color(0xFFEEF1F2),
    transcriptAgent = Color(0xFFF2F0FF),
    transcriptAgentBorder = Color(0xFFD1C8FF),
    transcriptClient = Color(0xFFEEF1F2),
    transcriptLive = Color(0xFF003FBD),
    highlightMatch = Color(0xFFFFF1C4),
    highlightCurrent = Color(0xFFFFCC61),
    track = Color(0xFFE6E9EB),
    trackFill = Color(0xFF6A41D2),
    isDark = false,
)

internal val DarkTalkdeskColors = TalkdeskColors(
    bgWindow = Color(0xFF1A1A1A),
    bgSubtle = Color(0xFF1A1A1A),
    surface1 = Color(0xFF1D2123),
    surface2 = Color(0xFF25292D),
    surface3 = Color(0xFF2B2F33),
    surfaceNav = Color(0xFF1D2123),
    surfaceSunken = Color(0xFF25292D),
    surfaceSelected = Color(0xFF1C1735),
    surfaceBrandSubtle = Color(0xFF2B2F33),
    surfaceKey = Color(0xFF25292D),
    skeletonBase = Color(0xFF25292D),
    skeletonHighlight = Color(0xFF2B2F33),
    scrim = Color(0xFF1A1A1A).copy(alpha = 0.72f),
    textDefault = Color(0xFFDEE2E5),
    textDescription = Color(0xFFB2B7BC),
    textPlaceholder = Color(0xFFB2B7BC),
    textDisabled = Color(0xFF606569),
    textBrand = Color(0xFFB5A5FF),
    textOnBrand = Color(0xFFFFFFFF),
    textOnFill = Color(0xFFFFFFFF),
    textOnLightFill = Color(0xFF1D2123),
    textOnDisabled = Color(0xFF7D8287),
    textError = Color(0xFFEE8D7F),
    textSuccess = Color(0xFF61B885),
    textWarning = Color(0xFFFFCC61),
    textInfo = Color(0xFFB8D3FF),
    borderControl = Color(0xFF7D8287),
    borderDivider = Color(0xFF2B2F33),
    borderFocus = Color(0xFFB5A5FF),
    borderError = Color(0xFFEE8D7F),
    borderSelected = Color(0xFFB5A5FF),
    iconDefault = Color(0xFFDEE2E5),
    iconMuted = Color(0xFFB2B7BC),
    iconBrand = Color(0xFFB5A5FF),
    actionPrimary = Color(0xFF5405BD),
    actionPrimaryPressed = Color(0xFF44009F),
    actionPrimaryDisabled = Color(0xFF2B2F33),
    actionCall = Color(0xFF00874F),
    actionCallPressed = Color(0xFF007942),
    actionEnd = Color(0xFFDB362A),
    actionReject = Color(0xFFCC2018),
    controlSelected = Color(0xFFB5A5FF),
    controlOnSelected = Color(0xFF1D2123),
    statusSuccess = Color(0xFF61B885),
    statusWarning = Color(0xFFFFCC61),
    statusError = Color(0xFFEE8D7F),
    statusInfo = Color(0xFFB8D3FF),
    fillSuccess = Color(0xFF00874F),
    fillError = Color(0xFFCC2018),
    fillWarning = Color(0xFFFFCC61),
    fillInfo = Color(0xFF003FBD),
    fillErrorSubtle = Color(0xFF25292D),
    fillSuccessSubtle = Color(0xFF25292D),
    fillWarningSubtle = Color(0xFF25292D),
    fillInfoSubtle = Color(0xFF25292D),
    presenceAvailable = Color(0xFF61B885),
    presenceBusy = Color(0xFFEE8D7F),
    presenceAway = Color(0xFFFFCC61),
    presenceOffline = Color(0xFF7D8287),
    callInbound = Color(0xFF61B885),
    callOutbound = Color(0xFFB5A5FF),
    callMissed = Color(0xFFEE8D7F),
    callstateIncoming = Color(0xFFB5A5FF),
    callstateDialing = Color(0xFFB8D3FF),
    callstateOncall = Color(0xFF61B885),
    callstateEmergency = Color(0xFFEE8D7F),
    callstateWrapup = Color(0xFFEE8D7F),
    qualityExcellent = Color(0xFF61B885),
    qualityGood = Color(0xFFFFCC61),
    qualityBad = Color(0xFFFF6300),
    badgeUnread = Color(0xFFFF6300),
    bubbleOutbound = Color(0xFF5405BD),
    bubbleInbound = Color(0xFF25292D),
    transcriptAgent = Color(0xFF1C1735),
    transcriptAgentBorder = Color(0xFF44009F),
    transcriptClient = Color(0xFF25292D),
    transcriptLive = Color(0xFFB8D3FF),
    highlightMatch = Color(0xFFFFDC8E),
    highlightCurrent = Color(0xFFEDA813),
    track = Color(0xFF2B2F33),
    trackFill = Color(0xFFB5A5FF),
    isDark = true,
)

internal val LocalTalkdeskColors = staticCompositionLocalOf { LightTalkdeskColors }
