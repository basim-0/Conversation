package org.talkdesk.conversation.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.unit.dp

/**
 * Maps Talkdesk tokens onto Material 3 so stock M3 widgets stay on-brand. Talkdesk components read
 * [TalkdeskTheme] directly; this mapping is only for M3 components used as-is.
 */
internal fun TalkdeskColors.toMaterialColorScheme(): ColorScheme {
    val base = if (isDark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = actionPrimary,
        onPrimary = textOnBrand,
        primaryContainer = surfaceBrandSubtle,
        onPrimaryContainer = textBrand,
        inversePrimary = textBrand,
        secondary = textBrand,
        onSecondary = textOnBrand,
        secondaryContainer = surfaceSelected,
        onSecondaryContainer = textBrand,
        tertiary = statusInfo,
        onTertiary = textOnFill,
        tertiaryContainer = fillInfoSubtle,
        onTertiaryContainer = textInfo,
        background = bgWindow,
        onBackground = textDefault,
        surface = surface1,
        onSurface = textDefault,
        surfaceVariant = surfaceSunken,
        onSurfaceVariant = textDescription,
        surfaceTint = actionPrimary,
        inverseSurface = textDefault,
        inverseOnSurface = surface1,
        error = textError,
        onError = textOnFill,
        errorContainer = fillErrorSubtle,
        onErrorContainer = textError,
        outline = borderControl,
        outlineVariant = borderDivider,
        scrim = scrim,
        surfaceBright = surface2,
        surfaceDim = surfaceSunken,
        surfaceContainerLowest = surface1,
        surfaceContainerLow = surface1,
        surfaceContainer = surface2,
        surfaceContainerHigh = surface2,
        surfaceContainerHighest = surface3,
    )
}

internal fun TalkdeskTypography.toMaterialTypography(): Typography = Typography(
    displayLarge = h1,
    displayMedium = h1,
    displaySmall = h2,
    headlineLarge = h1,
    headlineMedium = h2,
    headlineSmall = h3,
    titleLarge = h3,
    titleMedium = h4,
    titleSmall = bodyStrong,
    bodyLarge = body,
    bodyMedium = labelRegular,
    bodySmall = caption,
    labelLarge = bodyStrong,
    labelMedium = label,
    labelSmall = navLabel,
)

internal val TalkdeskMaterialShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)
