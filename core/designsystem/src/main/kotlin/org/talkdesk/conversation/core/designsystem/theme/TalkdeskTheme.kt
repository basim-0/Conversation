package org.talkdesk.conversation.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember

/**
 * The single app theme. Wrap every screen (in practice: the root content) in it:
 *
 * ```
 * setContent { TalkdeskTheme { AppNavHost() } }
 * ```
 *
 * Read tokens through [TalkdeskTheme.colors], [TalkdeskTheme.typography], [TalkdeskTheme.spacing],
 * [TalkdeskTheme.shapes] and [TalkdeskTheme.sizes]. A matching [MaterialTheme] is provided too, so
 * stock Material 3 components pick up Talkdesk colours, type and shapes.
 */
@Composable
fun TalkdeskTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkTalkdeskColors else LightTalkdeskColors
    val typography = DefaultTalkdeskTypography
    val colorScheme = remember(colors) { colors.toMaterialColorScheme() }
    val materialTypography = remember(typography) { typography.toMaterialTypography() }

    CompositionLocalProvider(
        LocalTalkdeskColors provides colors,
        LocalTalkdeskTypography provides typography,
        LocalTalkdeskSpacing provides TalkdeskSpacing(),
        LocalTalkdeskShapes provides TalkdeskShapes(),
        LocalTalkdeskSizes provides TalkdeskSizes(),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = materialTypography,
            shapes = TalkdeskMaterialShapes,
        ) {
            CompositionLocalProvider(
                LocalContentColor provides colors.textDefault,
                LocalTextStyle provides typography.body,
                content = content,
            )
        }
    }
}

/** Accessors for the current Talkdesk tokens. */
object TalkdeskTheme {
    val colors: TalkdeskColors
        @Composable @ReadOnlyComposable get() = LocalTalkdeskColors.current

    val typography: TalkdeskTypography
        @Composable @ReadOnlyComposable get() = LocalTalkdeskTypography.current

    val spacing: TalkdeskSpacing
        @Composable @ReadOnlyComposable get() = LocalTalkdeskSpacing.current

    val shapes: TalkdeskShapes
        @Composable @ReadOnlyComposable get() = LocalTalkdeskShapes.current

    val sizes: TalkdeskSizes
        @Composable @ReadOnlyComposable get() = LocalTalkdeskSizes.current
}
