package org.talkdesk.conversation.calling.impl.ui.dialer.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.talkdesk.conversation.calling.impl.R
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

/**
 * 24x16 flag of the dial-code button. Only the United States flag is drawn (the one the design shows);
 * any other country shows a globe until real flags exist (TEMPORARY, UI-only). Flag colours are content,
 * not theme tokens.
 */
@Composable
internal fun CountryFlag(isoCode: String, modifier: Modifier = Modifier) {
    if (isoCode == "US") {
        val description = stringResource(R.string.calling_flag_us)
        Canvas(
            modifier
                .size(24.dp, 16.dp)
                .clip(RoundedCornerShape(2.dp))
                .border(1.dp, TalkdeskTheme.colors.borderDivider, RoundedCornerShape(2.dp))
                .semantics { contentDescription = description },
        ) {
            val u = size.width / 24f
            drawRect(Color(0xFFB22234))
            listOf(1.23f, 3.69f, 6.15f, 8.62f, 11.08f, 13.54f).forEach { y ->
                drawRect(Color.White, topLeft = Offset(0f, y * u), size = Size(size.width, 1.23f * u))
            }
            drawRect(Color(0xFF3C3B6E), size = Size(10f * u, 8.6f * u))
        }
    } else {
        Icon(TalkdeskIcons.Globe, contentDescription = null, tint = TalkdeskTheme.colors.iconDefault, modifier = modifier.size(24.dp, 16.dp))
    }
}
