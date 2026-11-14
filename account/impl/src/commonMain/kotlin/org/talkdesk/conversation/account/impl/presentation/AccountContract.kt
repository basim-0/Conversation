package org.talkdesk.conversation.account.impl.presentation

import org.talkdesk.conversation.account.impl.presentation.model.AgentProfile
import org.talkdesk.conversation.account.impl.presentation.model.AgentStatus
import org.talkdesk.conversation.account.impl.presentation.model.StatusGroup

/**
 * Data contract of the Account tab. [AccountViewModel] owns an [AccountState] in one `StateFlow`,
 * receives [AccountIntent]s through `onIntent` and sends one-shot [AccountEffect]s.
 */
data class AccountState(
    val profile: AgentProfile = AgentProfile(name = "", initials = "", subtitle = ""),
    /** Account-defined statuses grouped by presence type, in the account's order. */
    val statusGroups: List<StatusGroup> = emptyList(),
    /** The status the agent has right now (optimistic while a change is in flight). */
    val selectedStatusId: String? = null,
    /** First load only: skeleton rows keep the layout. */
    val isLoading: Boolean = true,
    /** A status change is in flight: rows ignore taps, nothing else is blocked. */
    val isChangingStatus: Boolean = false,
    val dialog: AccountDialog? = null,
) {
    val selectedStatus: AgentStatus?
        get() = statusGroups.asSequence().flatMap { it.statuses }.firstOrNull { it.id == selectedStatusId }
}

sealed interface AccountDialog {
    /** "Log out?" with Cancel and Log out. */
    data object ConfirmLogOut : AccountDialog

    /** Logging out is not possible during an active call: single OK. */
    data object EndCallFirst : AccountDialog
}

sealed interface AccountIntent {
    data class SelectStatus(val statusId: String) : AccountIntent

    /** Undo on the "Status set" toast: go back to the previous status. */
    data object UndoStatusChange : AccountIntent

    /** Retry on the error toast: repeat the last choice. */
    data object RetryStatusChange : AccountIntent
    data object SendFeedback : AccountIntent
    data object LogOutClicked : AccountIntent
    data object ConfirmLogOut : AccountIntent
    data object DismissDialog : AccountIntent
}

sealed interface AccountEffect {
    /** Toast "Status set to [statusLabel]" with Undo (4 seconds). */
    data class StatusChanged(val statusLabel: String) : AccountEffect

    /** Error toast with Retry; stays until dismissed. */
    data object StatusChangeFailed : AccountEffect

    /** The agent confirmed log out: end the session. */
    data object SignOut : AccountEffect
}
