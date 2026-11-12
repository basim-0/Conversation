package org.talkdesk.conversation.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

/** Renders a preview in both themes, like the design-system cards. */
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
internal annotation class TalkdeskPreviews

@Composable
internal fun PreviewSurface(content: @Composable () -> Unit) {
    TalkdeskTheme {
        Box(Modifier.background(TalkdeskTheme.colors.bgWindow).padding(16.dp)) { content() }
    }
}
