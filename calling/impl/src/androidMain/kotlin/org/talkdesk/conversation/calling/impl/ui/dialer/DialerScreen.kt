package org.talkdesk.conversation.calling.impl.ui.dialer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.talkdesk.conversation.calling.impl.R
import org.talkdesk.conversation.calling.impl.presentation.dialer.DialerIntent
import org.talkdesk.conversation.calling.impl.presentation.dialer.DialerState
import org.talkdesk.conversation.calling.impl.ui.CallingPreviews
import org.talkdesk.conversation.calling.impl.ui.PreviewContact
import org.talkdesk.conversation.calling.impl.ui.rememberCallingHaptics
import org.talkdesk.conversation.calling.impl.ui.dialer.components.BackspaceButton
import org.talkdesk.conversation.calling.impl.ui.dialer.components.CallerIdLine
import org.talkdesk.conversation.calling.impl.ui.dialer.components.ContactSuggestion
import org.talkdesk.conversation.calling.impl.ui.dialer.components.CountryButton
import org.talkdesk.conversation.calling.impl.ui.dialer.components.NumberReadout
import org.talkdesk.conversation.core.designsystem.component.TalkdeskCallButton
import org.talkdesk.conversation.core.designsystem.component.TalkdeskCallButtonKind
import org.talkdesk.conversation.core.designsystem.component.TalkdeskDialpad
import org.talkdesk.conversation.core.designsystem.component.TalkdeskIconButton
import org.talkdesk.conversation.core.designsystem.component.TalkdeskToolbar
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

/**
 * New call: a toolbar with the contacts shortcut, the country and read-only caller ID on one row, the
 * number on its own full-width line, an optional matching contact, the design-system dial pad and, under
 * it, the round Call button (disabled until there is a number) with delete beside it.
 * Stateless: renders [state] and reports taps as [DialerIntent]s.
 */
@Composable
fun DialerScreen(
    state: DialerState,
    onIntent: (DialerIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TalkdeskTheme.colors
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val haptics = rememberCallingHaptics()
    Column(modifier.fillMaxSize().background(colors.bgWindow).statusBarsPadding().navigationBarsPadding()) {
        TalkdeskToolbar(
            title = stringResource(R.string.calling_new_call_title),
            navigationIcon = {
                TalkdeskIconButton(TalkdeskIcons.Back, stringResource(R.string.calling_back), onClick = { onIntent(DialerIntent.Back) })
            },
            actions = {
                TalkdeskIconButton(
                    icon = TalkdeskIcons.Contacts,
                    contentDescription = stringResource(R.string.calling_choose_contact),
                    onClick = { onIntent(DialerIntent.PickContact) },
                    tint = colors.iconBrand,
                )
            },
        )
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CountryButton(state.country, onClick = { onIntent(DialerIntent.PickCountry) })
            CallerIdLine(state.callerId, Modifier.weight(1f))
        }
        Spacer(Modifier.weight(1f))
        NumberReadout(
            formattedNumber = state.formattedNumber,
            onLongClick = {
                scope.launch {
                    val text = clipboard.getClipEntry()?.clipData?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.text
                    if (!text.isNullOrBlank()) onIntent(DialerIntent.PasteNumber(text.toString()))
                }
            },
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        if (state.isNumberInvalid) {
            Text(
                stringResource(R.string.calling_invalid_number),
                style = TalkdeskTheme.typography.caption,
                color = colors.textError,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
        state.suggestedContact?.let { contact ->
            ContactSuggestion(
                contact,
                onClick = { onIntent(DialerIntent.UseSuggestedContact) },
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
            )
        }
        Spacer(Modifier.weight(1f))
        TalkdeskDialpad(onKey = { onIntent(DialerIntent.KeyPressed(it)) }, modifier = Modifier.padding(horizontal = 24.dp))
        // Same three columns as the dial pad: Call under the middle column, delete under the right one.
        Row(
            Modifier.fillMaxWidth().height(120.dp).padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Spacer(Modifier.weight(1f))
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                TalkdeskCallButton(
                    kind = TalkdeskCallButtonKind.Call,
                    contentDescription = if (state.canCall) {
                        stringResource(R.string.calling_call_number, state.formattedNumber)
                    } else {
                        stringResource(R.string.calling_call)
                    },
                    onClick = {
                        haptics.confirm()
                        onIntent(DialerIntent.Call)
                    },
                    enabled = state.canCall,
                )
            }
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                DeleteKey(visible = state.digits.isNotEmpty(), onIntent = onIntent)
            }
        }
    }
}

/** Delete appears with the first digit. Separate function because inside a Row the scope picks the wrong overload. */
@Composable
private fun DeleteKey(visible: Boolean, onIntent: (DialerIntent) -> Unit) {
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
        BackspaceButton(
            onDelete = { onIntent(DialerIntent.DeleteDigit) },
            onClear = { onIntent(DialerIntent.ClearNumber) },
        )
    }
}

@CallingPreviews
@Composable
private fun DialerEmptyPreview() = TalkdeskTheme { DialerScreen(DialerState(), onIntent = {}) }

@CallingPreviews
@Composable
private fun DialerNumberEnteredPreview() = TalkdeskTheme {
    DialerScreen(
        DialerState(digits = "555014", formattedNumber = "(555) 014", suggestedContact = PreviewContact),
        onIntent = {},
    )
}

@CallingPreviews
@Composable
private fun DialerInvalidNumberPreview() = TalkdeskTheme {
    DialerScreen(DialerState(digits = "555", formattedNumber = "555", isNumberInvalid = true), onIntent = {})
}
