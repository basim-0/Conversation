package org.talkdesk.conversation.activity.impl.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import org.talkdesk.conversation.core.designsystem.component.TalkdeskButton
import org.talkdesk.conversation.core.designsystem.component.TalkdeskButtonVariant
import org.talkdesk.conversation.core.designsystem.component.TalkdeskEmptyState
import org.talkdesk.conversation.core.designsystem.component.TalkdeskListRowSkeleton
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme
import org.talkdesk.conversation.activity.impl.R
import org.talkdesk.conversation.activity.impl.presentation.ActivityChannel
import org.talkdesk.conversation.activity.impl.presentation.ActivityIntent
import org.talkdesk.conversation.activity.impl.presentation.ActivityTab

/** First load: skeleton rows with the real row geometry. */
@Composable
internal fun ActivityLoading(modifier: Modifier = Modifier) {
    val label = stringResource(R.string.activity_loading)
    Column(modifier.fillMaxSize().semantics { contentDescription = label }) {
        repeat(8) { TalkdeskListRowSkeleton() }
    }
}

/** Nothing to show: All, Missed, or filters that hide everything (with a way to clear them). */
@Composable
internal fun ActivityEmpty(
    tab: ActivityTab,
    selectedChannels: Set<ActivityChannel>,
    onIntent: (ActivityIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize().background(TalkdeskTheme.colors.bgWindow), contentAlignment = Alignment.Center) {
        when {
            tab == ActivityTab.Missed -> TalkdeskEmptyState(
                icon = TalkdeskIcons.Check,
                title = stringResource(R.string.activity_empty_missed_title),
                body = stringResource(R.string.activity_empty_missed_body),
            )
            selectedChannels.isNotEmpty() -> {
                val single = selectedChannels.singleOrNull()
                TalkdeskEmptyState(
                    icon = TalkdeskIcons.Search,
                    title = if (single != null) {
                        stringResource(R.string.activity_empty_channel_title, stringResource(single.labelRes()))
                    } else {
                        stringResource(R.string.activity_empty_filtered_title)
                    },
                    body = stringResource(R.string.activity_empty_filtered_body),
                    action = {
                        TalkdeskButton(
                            text = stringResource(R.string.activity_clear_filters),
                            onClick = { onIntent(ActivityIntent.ClearFilters) },
                            variant = TalkdeskButtonVariant.Ghost,
                        )
                    },
                )
            }
            else -> TalkdeskEmptyState(
                icon = TalkdeskIcons.Clock,
                title = stringResource(R.string.activity_empty_title),
                body = stringResource(R.string.activity_empty_body),
            )
        }
    }
}

/** First load failed and there is no cache. */
@Composable
internal fun ActivityFailed(onIntent: (ActivityIntent) -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().background(TalkdeskTheme.colors.bgWindow), contentAlignment = Alignment.Center) {
        TalkdeskEmptyState(
            icon = TalkdeskIcons.WifiOff,
            title = stringResource(R.string.activity_error_title),
            body = stringResource(R.string.activity_error_body),
            action = {
                TalkdeskButton(
                    text = stringResource(R.string.activity_retry),
                    onClick = { onIntent(ActivityIntent.Retry) },
                    variant = TalkdeskButtonVariant.Secondary,
                )
            },
        )
    }
}

internal fun ActivityChannel.labelRes(): Int = when (this) {
    ActivityChannel.Calls -> R.string.activity_channel_calls
    ActivityChannel.Sms -> R.string.activity_channel_sms
    ActivityChannel.WhatsApp -> R.string.activity_channel_whatsapp
    ActivityChannel.Email -> R.string.activity_channel_email
}
