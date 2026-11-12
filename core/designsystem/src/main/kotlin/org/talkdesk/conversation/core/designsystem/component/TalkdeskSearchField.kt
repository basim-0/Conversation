package org.talkdesk.conversation.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

/**
 * Inline search (`ta-search`): a 40dp `surface-sunken` box with a leading search icon,
 * `text-placeholder` hint and a clear button once text is typed. Pass [onCancel] to show the
 * borderless "Cancel" action that exits search.
 *
 * @param clearContentDescription accessibility label of the clear button (localise per platform).
 */
@Composable
fun TalkdeskSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    clearContentDescription: String,
    modifier: Modifier = Modifier,
    onSearch: (() -> Unit)? = null,
    cancelLabel: String? = null,
    onCancel: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = TalkdeskTheme.colors
    val typography = TalkdeskTheme.typography
    val shape = TalkdeskTheme.shapes.sm

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = typography.body.copy(color = colors.textDefault),
            cursorBrush = SolidColor(colors.borderFocus),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch?.invoke() }),
            decorationBox = { inner ->
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .background(colors.surfaceSunken, shape)
                        .padding(start = 12.dp, end = if (query.isEmpty()) 12.dp else 0.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(TalkdeskIcons.Search, null, tint = colors.iconMuted, modifier = Modifier.size(20.dp))
                    Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (query.isEmpty()) {
                            Text(placeholder, style = typography.body, color = colors.textPlaceholder, maxLines = 1)
                        }
                        inner()
                    }
                    if (query.isNotEmpty()) {
                        // 48dp touch target, as the design system requires for icon buttons.
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(TalkdeskIcons.X, clearContentDescription, tint = colors.iconMuted, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            },
        )
        trailing?.invoke()
        if (onCancel != null && cancelLabel != null) {
            TalkdeskButton(cancelLabel, onCancel, variant = TalkdeskButtonVariant.Ghost)
        }
    }
}

@TalkdeskPreviews
@Composable
private fun TalkdeskSearchFieldPreview() = PreviewSurface {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        var q by remember { mutableStateOf("") }
        TalkdeskSearchField(q, { q = it }, "Search contacts", "Clear")
        TalkdeskSearchField("Maria", {}, "Search contacts", "Clear", cancelLabel = "Cancel", onCancel = {})
    }
}
