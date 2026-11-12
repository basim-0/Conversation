package org.talkdesk.conversation.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Talkdesk type scale: Roboto (the Android system font) 400/500, in sp so text scales to 200%.
 * Read through [TalkdeskTheme.typography].
 */
@Immutable
data class TalkdeskTypography(
    /** 28/34 Medium. Main screen title (Contacts, Activity). */
    val h1: TextStyle,
    /** 24/30 Regular. Section hero titles, empty-state headline. */
    val h2: TextStyle,
    /** 20/26 Regular. Dialog titles. */
    val h3: TextStyle,
    /** 20/26 Medium. Contact name on call and profile screens. */
    val title: TextStyle,
    /** 18/24 Medium. Toolbar title, call contact number, sheet title. */
    val h4: TextStyle,
    /** 15/21 Medium. Buttons, list primary line, emphasised body. */
    val bodyStrong: TextStyle,
    /** 15/21 Regular. Paragraphs, input text, message bubbles. */
    val body: TextStyle,
    /** 14/20 Medium. Field labels, tab labels, selected chip label. */
    val label: TextStyle,
    /** 14/20 Regular. Chip label, secondary row text. */
    val labelRegular: TextStyle,
    /** 12/16 Regular. Timestamps, helper text. */
    val caption: TextStyle,
    /** 12/16 Medium. Section overline, uppercase, in textBrand. */
    val overline: TextStyle,
    /** 11/14 Medium. Bottom navigation label. */
    val navLabel: TextStyle,
    /** 24/30 Regular. Dialed number, dial-pad digits. */
    val numberDisplay: TextStyle,
    /** 18/24 Medium. Call timer (tabular figures). */
    val timer: TextStyle,
)

private val BaseStyle = TextStyle(
    fontFamily = FontFamily.Default,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None,
    ),
)

private fun style(
    size: Int,
    lineHeight: Int,
    weight: FontWeight,
    letterSpacing: TextUnit = 0.sp,
    fontFeatureSettings: String? = null,
) = BaseStyle.copy(
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    fontWeight = weight,
    letterSpacing = letterSpacing,
    fontFeatureSettings = fontFeatureSettings,
)

internal val DefaultTalkdeskTypography = TalkdeskTypography(
    h1 = style(28, 34, FontWeight.Medium),
    h2 = style(24, 30, FontWeight.Normal),
    h3 = style(20, 26, FontWeight.Normal),
    title = style(20, 26, FontWeight.Medium),
    h4 = style(18, 24, FontWeight.Medium),
    bodyStrong = style(15, 21, FontWeight.Medium, 0.1.sp),
    body = style(15, 21, FontWeight.Normal),
    label = style(14, 20, FontWeight.Medium, 0.1.sp),
    labelRegular = style(14, 20, FontWeight.Normal),
    caption = style(12, 16, FontWeight.Normal, 0.2.sp),
    overline = style(12, 16, FontWeight.Medium, 0.6.sp),
    navLabel = style(11, 14, FontWeight.Medium, 0.3.sp),
    numberDisplay = style(24, 30, FontWeight.Normal, 0.5.sp),
    timer = style(18, 24, FontWeight.Medium, 0.5.sp, fontFeatureSettings = "tnum"),
)

internal val LocalTalkdeskTypography = staticCompositionLocalOf { DefaultTalkdeskTypography }
