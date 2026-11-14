package org.talkdesk.conversation.activity.impl.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskElevation
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme
import org.talkdesk.conversation.core.ui.SharedTransitionKeys
import org.talkdesk.conversation.core.ui.sharedContainer
import org.talkdesk.conversation.core.designsystem.theme.talkdeskShadow
import org.talkdesk.conversation.activity.impl.R
import org.talkdesk.conversation.activity.impl.ui.ActivityPreviews

/**
 * "New call" extended FAB: `action-primary` fill, `text-on-brand`, `radius-lg`. Collapses to the
 * 56dp icon button when [expanded] is false (the screen collapses it after 24dp of scroll).
 *
 * Private to the activity feature by decision: the design system does not have this component yet
 * (it is only proposed in the revamp spec). Move it to `core:designsystem` once designed there.
 */
@Composable
internal fun NewCallFab(
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TalkdeskTheme.colors
    ExtendedFloatingActionButton(
        onClick = onClick,
        expanded = expanded,
        modifier = modifier
            // The dialer expands out of this button when it opens.
            .sharedContainer(SharedTransitionKeys.NewCall, TalkdeskTheme.shapes.lg)
            .talkdeskShadow(TalkdeskElevation.Overlay, TalkdeskTheme.shapes.lg),
        shape = TalkdeskTheme.shapes.lg,
        containerColor = colors.actionPrimary,
        contentColor = colors.textOnBrand,
        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
        icon = { Icon(TalkdeskIcons.Phone, contentDescription = null) },
        text = { Text(stringResource(R.string.activity_new_call), style = TalkdeskTheme.typography.bodyStrong) },
    )
}

@ActivityPreviews
@Composable
private fun NewCallFabPreview() = TalkdeskTheme {
    Column(Modifier.background(TalkdeskTheme.colors.bgWindow).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        NewCallFab(expanded = true, onClick = {})
        NewCallFab(expanded = false, onClick = {})
    }
}
