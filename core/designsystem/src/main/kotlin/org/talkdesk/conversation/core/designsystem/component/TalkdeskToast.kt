package org.talkdesk.conversation.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskColors
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskElevation
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme
import org.talkdesk.conversation.core.designsystem.theme.talkdeskShadow

enum class TalkdeskToastVariant { Success, Primary, Error, Warning, Info, Neutral }

enum class TalkdeskBannerVariant { Success, Error, Warning, Info }

/**
 * Transient feedback (`ta-toast`): `radius-sm`, `shadow-overlay`, at most 360dp wide, one optional
 * action. Every toast carries an icon and words, never colour alone; [icon] defaults per variant.
 * Show it through [TalkdeskSnackbarHost] + [showTalkdeskToast].
 */
@Composable
fun TalkdeskToast(
    message: String,
    modifier: Modifier = Modifier,
    variant: TalkdeskToastVariant = TalkdeskToastVariant.Neutral,
    icon: ImageVector = variant.defaultIcon(),
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = TalkdeskTheme.colors
    val shape = TalkdeskTheme.shapes.sm
    val (container, content) = variant.colors(colors)
    Row(
        modifier = modifier
            .widthIn(max = 360.dp)
            .defaultMinSize(minHeight = 48.dp)
            .talkdeskShadow(TalkdeskElevation.Overlay, shape)
            .background(container, shape)
            .then(if (variant == TalkdeskToastVariant.Neutral) Modifier.border(1.dp, colors.borderDivider, shape) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(24.dp))
        Text(message, style = TalkdeskTheme.typography.body, color = content, modifier = Modifier.weight(1f, fill = false))
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                style = TalkdeskTheme.typography.bodyStrong.copy(textDecoration = TextDecoration.Underline),
                color = content,
                modifier = Modifier
                    .defaultMinSize(minHeight = 36.dp)
                    .clickable(role = Role.Button, onClick = onAction)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
            )
        }
    }
}

/** Snackbar payload that carries the toast variant. */
data class TalkdeskToastVisuals(
    override val message: String,
    val variant: TalkdeskToastVariant = TalkdeskToastVariant.Neutral,
    override val actionLabel: String? = null,
    override val duration: SnackbarDuration =
        if (actionLabel == null) SnackbarDuration.Short else SnackbarDuration.Long,
    override val withDismissAction: Boolean = false,
    /** Overrides the variant's default icon. */
    val icon: ImageVector? = null,
) : SnackbarVisuals

/** Shows a Talkdesk toast in the [TalkdeskSnackbarHost] that observes this state. */
suspend fun SnackbarHostState.showTalkdeskToast(
    message: String,
    variant: TalkdeskToastVariant = TalkdeskToastVariant.Neutral,
    actionLabel: String? = null,
    icon: ImageVector? = null,
    duration: SnackbarDuration = if (actionLabel == null) SnackbarDuration.Short else SnackbarDuration.Long,
): SnackbarResult = showSnackbar(
    TalkdeskToastVisuals(message, variant, actionLabel, duration = duration, icon = icon),
)

/**
 * A [SnackbarHost] that renders every snackbar as a [TalkdeskToast], full width up to the toast's
 * 360dp maximum. Place it in a Scaffold's `snackbarHost`, or align it in a Box. [contentPadding] is the
 * space around the toast; apply window insets through [modifier] when it sits at a screen edge.
 */
@Composable
fun TalkdeskSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
) {
    SnackbarHost(hostState, modifier) { data ->
        val visuals = data.visuals
        val toast = visuals as? TalkdeskToastVisuals
        val variant = toast?.variant ?: TalkdeskToastVariant.Neutral
        TalkdeskToast(
            message = visuals.message,
            variant = variant,
            icon = toast?.icon ?: variant.defaultIcon(),
            actionLabel = visuals.actionLabel,
            onAction = data::performAction,
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxWidth(),
        )
    }
}

/**
 * Persistent inline message (`ta-banner`) on a subtle fill with the matching text colour. Use for
 * wrap-up timers and recording state. [title] is emphasised; [message] is in `text-default`.
 */
@Composable
fun TalkdeskBanner(
    title: String,
    modifier: Modifier = Modifier,
    variant: TalkdeskBannerVariant = TalkdeskBannerVariant.Info,
    message: String? = null,
    icon: ImageVector = when (variant) {
        TalkdeskBannerVariant.Success -> TalkdeskIcons.Check
        TalkdeskBannerVariant.Info -> TalkdeskIcons.Info
        TalkdeskBannerVariant.Error, TalkdeskBannerVariant.Warning -> TalkdeskIcons.Alert
    },
) {
    val c = TalkdeskTheme.colors
    val (container, content) = when (variant) {
        TalkdeskBannerVariant.Success -> c.fillSuccessSubtle to c.textSuccess
        TalkdeskBannerVariant.Error -> c.fillErrorSubtle to c.textError
        TalkdeskBannerVariant.Warning -> c.fillWarningSubtle to c.textWarning
        TalkdeskBannerVariant.Info -> c.fillInfoSubtle to c.textInfo
    }
    val typography = TalkdeskTheme.typography
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(container, TalkdeskTheme.shapes.sm)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(20.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = typography.label, color = content)
            if (message != null) Text(message, style = typography.labelRegular, color = c.textDefault)
        }
    }
}

@Composable
private fun TalkdeskToastVariant.defaultIcon(): ImageVector = when (this) {
    TalkdeskToastVariant.Success -> TalkdeskIcons.Check
    TalkdeskToastVariant.Error, TalkdeskToastVariant.Warning -> TalkdeskIcons.Alert
    TalkdeskToastVariant.Primary, TalkdeskToastVariant.Info, TalkdeskToastVariant.Neutral -> TalkdeskIcons.Info
}

private fun TalkdeskToastVariant.colors(c: TalkdeskColors): Pair<Color, Color> = when (this) {
    TalkdeskToastVariant.Success -> c.fillSuccess to c.textOnFill
    TalkdeskToastVariant.Primary -> c.actionPrimary to c.textOnBrand
    TalkdeskToastVariant.Error -> c.fillError to c.textOnFill
    TalkdeskToastVariant.Warning -> c.fillWarning to c.textOnLightFill
    TalkdeskToastVariant.Info -> c.fillInfo to c.textOnFill
    TalkdeskToastVariant.Neutral -> c.surface2 to c.textDefault
}

@TalkdeskPreviews
@Composable
private fun TalkdeskToastPreview() = PreviewSurface {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TalkdeskToast("Call transferred", variant = TalkdeskToastVariant.Success)
        TalkdeskToast("Status set to Available", variant = TalkdeskToastVariant.Primary)
        TalkdeskToast("Couldn't send message", variant = TalkdeskToastVariant.Error, actionLabel = "Retry", onAction = {})
        TalkdeskToast("Weak connection", variant = TalkdeskToastVariant.Warning)
        TalkdeskToast("Recording started", variant = TalkdeskToastVariant.Info)
        TalkdeskToast("Copied to clipboard")
        TalkdeskBanner("Wrap-up ends in 0:45", variant = TalkdeskBannerVariant.Warning, message = "Choose a disposition to finish.")
        TalkdeskBanner("Call is being recorded", variant = TalkdeskBannerVariant.Info)
    }
}
