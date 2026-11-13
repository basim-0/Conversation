package org.talkdesk.conversation.auth.android.login.components

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.talkdesk.conversation.auth.android.R
import org.talkdesk.conversation.auth.android.login.splashscreen.LocalAppMarkHandoff
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

/** UX spec: on tablets and in landscape the login form sits in a centred column, max 400dp wide. */
internal val LoginFormMaxWidth = 400.dp

/**
 * App mark: an `action-primary` rounded square (`radius-md`) with the chat-bubble glyph
 * ([AppMarkGlyph]) in `text-on-brand`. Placeholder until the official Talkdesk Conversations icon
 * exists (design open question 5).
 *
 * @param large 56dp mark with a 32dp glyph ("Signing you in"); otherwise 40dp with a 24dp glyph.
 */
@Composable
internal fun AppMark(modifier: Modifier = Modifier, large: Boolean = false) {
    val colors = TalkdeskTheme.colors
    val sizes = TalkdeskTheme.sizes
    val glyph = if (large) TalkdeskTheme.spacing.space32 else sizes.icon
    // radius-md is 8dp; the shape token can't be drawn on a Canvas, so use its value.
    val radius = TalkdeskTheme.spacing.space8
    Canvas(modifier.size(if (large) sizes.control else sizes.avatar)) {
        drawRoundRect(colors.actionPrimary, cornerRadius = CornerRadius(radius.toPx()))
        val side = glyph.toPx()
        drawAppMarkGlyph(
            topLeft = Offset((size.width - side) / 2, (size.height - side) / 2),
            side = side,
            color = colors.textOnBrand,
        )
    }
}

/**
 * Header of the login screens: [AppMark] followed by the app name. Inside a [org.talkdesk.conversation.auth.android.login.splashscreen.SplashTransition] the
 * mark reports where it sits and stays hidden until the splash mark has landed on it.
 */
@Composable
internal fun AppLockup(modifier: Modifier = Modifier) {
    val handoff = LocalAppMarkHandoff.current
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TalkdeskTheme.spacing.space12),
    ) {
        AppMark(
            if (handoff == null) {
                Modifier
            } else {
                Modifier
                    .onGloballyPositioned { handoff.markCoordinates = it }
                    .graphicsLayer { alpha = if (handoff.markHidden) 0f else 1f }
            },
        )
        Text(
            text = stringResource(R.string.auth_app_name),
            style = TalkdeskTheme.typography.bodyStrong,
            color = TalkdeskTheme.colors.textDefault,
        )
    }
}

/**
 * Shared frame of the login form screens: `bg-window`, a scrolling column inside the safe area
 * (16dp sides, 24dp below the status bar), the [AppLockup], [title] in `h1` and [description].
 * [content] follows 24dp below the description. [overlay] is drawn over the screen (snackbars).
 */
@Composable
internal fun LoginFormLayout(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    overlay: @Composable BoxScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = TalkdeskTheme.colors
    val spacing = TalkdeskTheme.spacing
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bgWindow),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(start = spacing.space16, end = spacing.space16, top = spacing.space24, bottom = spacing.space16),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(Modifier.widthIn(max = LoginFormMaxWidth).fillMaxWidth()) {
                AppLockup()
                Text(
                    text = title,
                    style = TalkdeskTheme.typography.h1,
                    color = colors.textDefault,
                    modifier = Modifier
                        .padding(top = spacing.space32, bottom = spacing.space8)
                        .semantics { heading() },
                )
                Text(description, style = TalkdeskTheme.typography.body, color = colors.textDescription)
                Spacer(Modifier.height(spacing.space24))
                content()
            }
        }
        overlay()
    }
}

/** Remembered account inside a card: globe badge, account name and its web address. */
@Composable
internal fun RememberedAccountRow(accountName: String, modifier: Modifier = Modifier) {
    val colors = TalkdeskTheme.colors
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TalkdeskTheme.spacing.space12),
    ) {
        Box(
            modifier = Modifier
                .size(TalkdeskTheme.sizes.avatar)
                .background(colors.surfaceBrandSubtle, TalkdeskTheme.shapes.full),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = TalkdeskIcons.Globe,
                contentDescription = null,
                tint = colors.iconBrand,
                modifier = Modifier.size(TalkdeskTheme.sizes.icon),
            )
        }
        Column {
            Text(accountName, style = TalkdeskTheme.typography.bodyStrong, color = colors.textDefault)
            Text(
                text = stringResource(R.string.auth_account_host, accountName),
                style = TalkdeskTheme.typography.caption,
                color = colors.textDescription,
            )
        }
    }
}

/** Phone-sized preview in both themes, matching the 360×800 Android board frames. */
@Preview(name = "Light", widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES)
internal annotation class LoginPreviews
