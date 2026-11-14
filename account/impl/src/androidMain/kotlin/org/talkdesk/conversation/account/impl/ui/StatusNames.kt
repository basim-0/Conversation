package org.talkdesk.conversation.account.impl.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import org.talkdesk.conversation.account.impl.R
import org.talkdesk.conversation.account.impl.domain.model.StatusType
import org.talkdesk.conversation.account.impl.presentation.model.AgentStatus

/** Name shown for a status: the account's own label for custom statuses, a string resource for the rest. */
internal fun AgentStatus.displayName(context: Context): String = label ?: context.getString(
    when (kind) {
        StatusType.Available -> R.string.account_presence_available
        StatusType.Away -> R.string.account_presence_away
        StatusType.Offline -> R.string.account_presence_offline
        StatusType.Busy, StatusType.Unknown -> R.string.account_presence_busy
        StatusType.AfterCallWork -> R.string.account_status_after_call_work
    },
)

@Composable
internal fun AgentStatus.displayName(): String = displayName(LocalContext.current)
