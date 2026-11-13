package org.talkdesk.conversation.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

/**
 * Empty or not-found state (`ta-empty`): a 120dp token-built illustration, an `h4` title, one
 * sentence of guidance and at most one [action].
 */
@Composable
fun TalkdeskEmptyState(
    icon: ImageVector,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    val colors = TalkdeskTheme.colors
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.padding(bottom = 8.dp).size(120.dp)) {
            Box(Modifier.fillMaxSize().background(colors.surfaceBrandSubtle, TalkdeskTheme.shapes.full))
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = 10.dp)
                    .size(28.dp)
                    .background(colors.transcriptAgentBorder, TalkdeskTheme.shapes.full),
            )
            Icon(icon, contentDescription = null, tint = colors.iconBrand, modifier = Modifier.align(Alignment.Center).size(48.dp))
        }
        Text(title, style = TalkdeskTheme.typography.h4, color = colors.textDefault, textAlign = TextAlign.Center)
        Text(
            body,
            style = TalkdeskTheme.typography.body,
            color = colors.textDescription,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 280.dp),
        )
        action?.invoke()
    }
}

private fun Modifier.fillMaxSize(): Modifier = this.size(120.dp)

@TalkdeskPreviews
@Composable
private fun TalkdeskEmptyStatePreview() = PreviewSurface {
    TalkdeskEmptyState(
        icon = TalkdeskIcons.Search,
        title = "No contacts found",
        body = "Check the spelling or search by phone number.",
        action = { TalkdeskButton("New contact", {}, variant = TalkdeskButtonVariant.Secondary, leadingIcon = TalkdeskIcons.AddUser) },
    )
}
