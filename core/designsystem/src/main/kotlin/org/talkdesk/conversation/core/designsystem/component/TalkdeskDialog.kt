package org.talkdesk.conversation.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskElevation
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme
import org.talkdesk.conversation.core.designsystem.theme.talkdeskShadow

/**
 * Centred modal (`ta-dialog`): `surface-2`, `radius-md`, 24 padding, 312dp wide on phones. Title in
 * `h3`, body in `text-description`, right-aligned actions with the confirming action last.
 *
 * Actions are usually [TalkdeskButton]s with [TalkdeskButtonVariant.Ghost]; permission dialogs pass
 * an [icon] and use a Primary button to continue to the system prompt; irreversible actions use
 * [TalkdeskButtonVariant.Destructive].
 */
@Composable
fun TalkdeskDialog(
    onDismissRequest: () -> Unit,
    title: String,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    dismissButton: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
) {
    Dialog(onDismissRequest = onDismissRequest, properties = properties) {
        // Match the design-system scrim strength (light 50%, dark 72%).
        val window = (LocalView.current.parent as? DialogWindowProvider)?.window
        val dark = TalkdeskTheme.colors.isDark
        SideEffect { window?.setDimAmount(if (dark) 0.72f else 0.5f) }
        TalkdeskDialogContent(title, confirmButton, modifier, text, dismissButton, icon)
    }
}

/** The dialog card on its own, for custom presentation and previews. */
@Composable
fun TalkdeskDialogContent(
    title: String,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    dismissButton: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
) {
    val colors = TalkdeskTheme.colors
    val shape = TalkdeskTheme.shapes.md
    Column(
        modifier = modifier
            .padding(horizontal = 24.dp)
            .widthIn(max = 312.dp)
            .fillMaxWidth()
            .talkdeskShadow(TalkdeskElevation.Overlay, shape)
            .background(colors.surface2, shape)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (icon != null) {
            Box(
                Modifier.size(40.dp).background(colors.surfaceBrandSubtle, TalkdeskTheme.shapes.full),
                contentAlignment = Alignment.Center,
            ) {
                CompositionLocalProvider(LocalContentColor provides colors.iconBrand) { icon() }
            }
        }
        Text(title, style = TalkdeskTheme.typography.h3, color = colors.textDefault)
        if (text != null) {
            Text(text, style = TalkdeskTheme.typography.body, color = colors.textDescription)
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        ) {
            dismissButton?.invoke()
            confirmButton()
        }
    }
}

@TalkdeskPreviews
@Composable
private fun TalkdeskDialogPreview() = PreviewSurface {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TalkdeskDialogContent(
            title = "End call for everyone?",
            text = "All participants will be disconnected.",
            dismissButton = { TalkdeskButton("Cancel", {}, variant = TalkdeskButtonVariant.Ghost) },
            confirmButton = { TalkdeskButton("End for all", {}, variant = TalkdeskButtonVariant.Destructive) },
        )
        TalkdeskDialogContent(
            title = "Allow microphone access",
            text = "Talkdesk needs the microphone to make and receive calls.",
            icon = { Icon(TalkdeskIcons.Mic, null) },
            dismissButton = { TalkdeskButton("Not now", {}, variant = TalkdeskButtonVariant.Ghost) },
            confirmButton = { TalkdeskButton("Continue", {}) },
        )
    }
}
