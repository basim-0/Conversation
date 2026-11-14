package org.talkdesk.conversation.calling.impl.ui.dialer.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import org.talkdesk.conversation.calling.impl.R
import org.talkdesk.conversation.calling.impl.ui.CallingPreviews
import org.talkdesk.conversation.calling.impl.ui.PreviewSurface
import org.talkdesk.conversation.calling.impl.ui.rememberCallingHaptics
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

/**
 * Delete key next to the Call button. Tap deletes the last digit; a long press clears the whole number
 * (also exposed as an accessibility action). The design system's icon button has no long press, so this
 * stays private to the calling feature.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun BackspaceButton(onDelete: () -> Unit, onClear: () -> Unit, modifier: Modifier = Modifier) {
    val haptics = rememberCallingHaptics()
    val deleteLabel = stringResource(R.string.calling_delete_digit)
    val clearLabel = stringResource(R.string.calling_clear_number)
    Box(
        modifier
            .size(TalkdeskTheme.sizes.touch)
            .clip(TalkdeskTheme.shapes.full)
            .combinedClickable(
                role = Role.Button,
                onClick = {
                    haptics.tick()
                    onDelete()
                },
                onLongClick = {
                    haptics.longPress()
                    onClear()
                },
            )
            .semantics {
                contentDescription = deleteLabel
                customActions = listOf(CustomAccessibilityAction(clearLabel) { onClear(); true })
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            TalkdeskIcons.Backspace,
            contentDescription = null,
            tint = TalkdeskTheme.colors.iconDefault,
            modifier = Modifier.size(TalkdeskTheme.sizes.icon),
        )
    }
}

@CallingPreviews
@Composable
private fun BackspaceButtonPreview() = PreviewSurface { BackspaceButton(onDelete = {}, onClear = {}) }
