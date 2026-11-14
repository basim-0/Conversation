package org.talkdesk.conversation.calling.impl.ui.dialer.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import org.talkdesk.conversation.calling.impl.R
import org.talkdesk.conversation.calling.impl.ui.CallingPreviews
import org.talkdesk.conversation.calling.impl.ui.PreviewSurface
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

/** How much bigger than `number-display` the number starts, and how far it shrinks, as the agent types. */
private const val MaxScale = 1.5f
private const val MinScale = 0.6f

/**
 * The number being dialed, centered on its own full-width line. It starts at 1.5x `number-display` and
 * shrinks smoothly (down to 0.6x) so a whole number always fits on one line. Empty it shows the
 * "Enter a number" placeholder. A long press asks for a paste ([onLongClick]).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun NumberReadout(formattedNumber: String, onLongClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = TalkdeskTheme.colors
    // Sizes derive from the theme token; line height is left to the auto-sizer so it scales with the text.
    val base = TalkdeskTheme.typography.numberDisplay
    val style = base.copy(lineHeight = TextUnit.Unspecified, textAlign = TextAlign.Center)
    Box(
        modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .combinedClickable(onClick = {}, onLongClick = onLongClick),
        contentAlignment = Alignment.Center,
    ) {
        if (formattedNumber.isEmpty()) {
            BasicText(
                stringResource(R.string.calling_enter_number),
                style = style.copy(color = colors.textPlaceholder),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        } else {
            BasicText(
                formattedNumber,
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = formattedNumber.toList().joinToString(" ") },
                style = style.copy(color = colors.textDefault),
                maxLines = 1,
                overflow = TextOverflow.Clip,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = base.fontSize * MinScale,
                    maxFontSize = base.fontSize * MaxScale,
                ),
            )
        }
    }
}

@CallingPreviews
@Composable
private fun NumberReadoutPreview() = PreviewSurface {
    Column {
        NumberReadout("", onLongClick = {})
        NumberReadout("(555) 014", onLongClick = {})
        NumberReadout("(555) 014-2345678901", onLongClick = {})
    }
}
