package org.talkdesk.conversation.account.impl.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.talkdesk.conversation.account.impl.presentation.model.PresenceKind
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme
import org.talkdesk.conversation.account.impl.ui.AccountPreviews
import org.talkdesk.conversation.account.impl.ui.PreviewSurface

/**
 * 12dp presence mark of the Account design: disc (available), 3dp ring (away), 2dp ring (offline) and
 * a rounded square (busy / set by the system). Shape and colour together, never colour alone.
 *
 * Private to the account feature: it differs from the design-system avatar presence dot (10dp, square
 * corner 2dp) and the account board specifies these exact values.
 */
@Composable
internal fun StatusIndicator(presence: PresenceKind, modifier: Modifier = Modifier, size: Dp = 12.dp) {
    val colors = TalkdeskTheme.colors
    val circle = TalkdeskTheme.shapes.full
    val base = modifier.size(size)
    when (presence) {
        PresenceKind.Available -> Box(base.background(colors.presenceAvailable, circle))
        PresenceKind.Away -> Box(base.border(3.dp, colors.presenceAway, circle))
        PresenceKind.Offline -> Box(base.border(2.dp, colors.presenceOffline, circle))
        PresenceKind.Busy -> Box(base.background(colors.presenceBusy, RoundedCornerShape(3.dp)))
    }
}

@AccountPreviews
@Composable
private fun StatusIndicatorPreview() = PreviewSurface {
    Row { PresenceKind.entries.forEach { StatusIndicator(it, Modifier.padding(end = 16.dp)) } }
}
