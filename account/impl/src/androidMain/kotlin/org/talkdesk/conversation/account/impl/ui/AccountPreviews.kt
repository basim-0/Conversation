package org.talkdesk.conversation.account.impl.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

/** Phone-sized preview in both themes, matching the 360×800 Android board frames. */
@Preview(name = "Light", widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES)
internal annotation class AccountPreviews

/** Themed surface for previews of single components. */
@Composable
internal fun PreviewSurface(content: @Composable () -> Unit) {
    TalkdeskTheme {
        Box(Modifier.background(TalkdeskTheme.colors.bgWindow).padding(16.dp)) { content() }
    }
}
