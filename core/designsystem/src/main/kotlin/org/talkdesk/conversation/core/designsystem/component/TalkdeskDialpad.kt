package org.talkdesk.conversation.core.designsystem.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

private data class DialKey(val digit: Char, val letters: String)

private val DialKeys = listOf(
    listOf(DialKey('1', ""), DialKey('2', "ABC"), DialKey('3', "DEF")),
    listOf(DialKey('4', "GHI"), DialKey('5', "JKL"), DialKey('6', "MNO")),
    listOf(DialKey('7', "PQRS"), DialKey('8', "TUV"), DialKey('9', "WXYZ")),
    listOf(DialKey('*', ""), DialKey('0', "+"), DialKey('#', "")),
)

/**
 * The 3x4 key grid of the dialer and the in-call DTMF sheet (`ta-dialpad`). Keys are 64dp high on
 * `surface-key` with a `border-divider` outline and `radius-sm`, 10dp apart; pressed `surface-sunken`.
 * [onKey] gets the digit; a long press on 0 sends '+' instead.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TalkdeskDialpad(
    onKey: (Char) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        DialKeys.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { key -> DialpadKey(key, onKey, Modifier.weight(1f)) }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DialpadKey(key: DialKey, onKey: (Char) -> Unit, modifier: Modifier = Modifier) {
    val colors = TalkdeskTheme.colors
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current
    val shape = TalkdeskTheme.shapes.sm
    val longPressPlus = key.digit == '0'
    Column(
        modifier
            .height(TalkdeskTheme.sizes.dialpadKey)
            .clip(shape)
            .background(if (pressed) colors.surfaceSunken else colors.surfaceKey, shape)
            .border(1.dp, colors.borderDivider, shape)
            .combinedClickable(
                interactionSource = source,
                indication = null,
                role = Role.Button,
                onLongClick = if (longPressPlus) {
                    {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onKey('+')
                    }
                } else {
                    null
                },
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onKey(key.digit)
                },
            )
            .semantics { contentDescription = key.digit.toString() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(key.digit.toString(), style = TalkdeskTheme.typography.numberDisplay, color = colors.textDefault)
        // Letters line is always laid out so every key has the same text block height.
        Text(
            text = key.letters.ifEmpty { " " },
            style = TalkdeskTheme.typography.navLabel.copy(letterSpacing = 1.sp),
            color = colors.textDescription,
            textAlign = TextAlign.Center,
        )
    }
}

@TalkdeskPreviews
@Composable
private fun TalkdeskDialpadPreview() = PreviewSurface {
    Column(Modifier.padding(horizontal = 8.dp)) { TalkdeskDialpad(onKey = {}) }
}
