package org.talkdesk.conversation.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

/**
 * Top app bar (`ta-toolbar`): 56dp on `bg-window`, optional [navigationIcon] (back or close), title in
 * `h4`, and up to two [actions] (48dp targets). Insets are the caller's responsibility.
 */
@Composable
fun TalkdeskToolbar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val colors = TalkdeskTheme.colors
    Row(
        modifier
            .fillMaxWidth()
            .height(TalkdeskTheme.sizes.toolbar)
            .background(colors.bgWindow)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        navigationIcon?.invoke()
        Text(
            title,
            style = TalkdeskTheme.typography.h4,
            color = colors.textDefault,
            modifier = Modifier
                .weight(1f)
                .padding(start = if (navigationIcon == null) 12.dp else 4.dp)
                .semantics { heading() },
        )
        actions()
    }
}

/**
 * Root-screen toolbar (`ta-toolbar--large`): 64dp row with the `h1` title and trailing [actions].
 * Matches the Activity title row.
 */
@Composable
fun TalkdeskLargeToolbar(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val colors = TalkdeskTheme.colors
    Row(
        modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(start = 16.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            style = TalkdeskTheme.typography.h1,
            color = colors.textDefault,
            modifier = Modifier.weight(1f).semantics { heading() },
        )
        actions()
    }
}

@TalkdeskPreviews
@Composable
private fun TalkdeskToolbarPreview() = PreviewSurface {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TalkdeskToolbar(
            title = "New call",
            navigationIcon = { TalkdeskIconButton(TalkdeskIcons.Back, "Back", {}) },
        )
        TalkdeskToolbar(
            title = "Contact",
            navigationIcon = { TalkdeskIconButton(TalkdeskIcons.Back, "Back", {}) },
            actions = {
                TalkdeskIconButton(TalkdeskIcons.Edit, "Edit", {})
                TalkdeskIconButton(TalkdeskIcons.More, "More", {})
            },
        )
        TalkdeskLargeToolbar(title = "Account") { TalkdeskIconButton(TalkdeskIcons.Message, "Send feedback", {}) }
    }
}
