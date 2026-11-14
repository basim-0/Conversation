package org.talkdesk.conversation.account.impl.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

/**
 * State holder of the Account tab. UI only for now: the profile and statuses are mock data
 * ([AccountMock]) and a status change always succeeds, so the toast and error paths are wired through
 * [AccountEffect] but nothing raises the error yet.
 */
class AccountViewModel : ViewModel() {

    private val _state = MutableStateFlow(
        AccountState(
            profile = AccountMock.profile,
            statusGroups = AccountMock.statusGroups,
            selectedStatusId = AccountMock.AVAILABLE,
            isLoading = false,
        ),
    )
    val state: StateFlow<AccountState> = _state.asStateFlow()

    private val _effects = Channel<AccountEffect>(Channel.BUFFERED)
    val effects: Flow<AccountEffect> = _effects.receiveAsFlow()

    private var previousStatusId: String? = null
    private var lastChoiceId: String? = null

    fun onIntent(intent: AccountIntent) {
        when (intent) {
            is AccountIntent.SelectStatus -> changeStatus(intent.statusId)
            AccountIntent.UndoStatusChange -> previousStatusId?.let { applyStatus(it, announce = false) }
            AccountIntent.RetryStatusChange -> lastChoiceId?.let(::changeStatus)
            AccountIntent.LogOutClicked -> _state.update { it.copy(dialog = AccountDialog.ConfirmLogOut) }
            AccountIntent.DismissDialog -> _state.update { it.copy(dialog = null) }
            AccountIntent.ConfirmLogOut -> {
                _state.update { it.copy(dialog = null) }
                _effects.trySend(AccountEffect.SignOut)
            }
            // No destination is designed for feedback yet.
            AccountIntent.SendFeedback -> Unit
        }
    }

    private fun changeStatus(statusId: String) {
        val current = _state.value
        val target = current.statusGroups.flatMap { it.statuses }.firstOrNull { it.id == statusId }
        if (target == null || target.isSystemSet || current.isChangingStatus || statusId == current.selectedStatusId) return
        previousStatusId = current.selectedStatusId
        lastChoiceId = statusId
        applyStatus(statusId, announce = true)
    }

    private fun applyStatus(statusId: String, announce: Boolean) {
        _state.update { it.copy(selectedStatusId = statusId) }
        if (announce) {
            val label = _state.value.selectedStatus?.label ?: return
            _effects.trySend(AccountEffect.StatusChanged(label))
        }
    }
}
