package org.talkdesk.conversation.calling.impl.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme
import org.talkdesk.conversation.calling.impl.presentation.CallContact

/** Phone-sized preview in both themes, matching the 360×800 Android board frames. */
@Preview(name = "Light", widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES)
internal annotation class CallingPreviews

/** Sample contact for the Compose previews only; the running app gets its state from the ViewModels. */
internal val PreviewContact = CallContact(name = "Maria Silva", initials = "MS", phoneType = "Mobile", number = "+1 555 0142")

/** Themed surface for previews of single components. */
@Composable
internal fun PreviewSurface(content: @Composable () -> Unit) {
    TalkdeskTheme {
        Box(Modifier.background(TalkdeskTheme.colors.bgWindow).padding(16.dp)) { content() }
    }
}
