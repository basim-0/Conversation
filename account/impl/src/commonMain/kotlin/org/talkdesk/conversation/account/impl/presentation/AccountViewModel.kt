package org.talkdesk.conversation.account.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.talkdesk.conversation.account.impl.domain.AccountRepository
import org.talkdesk.conversation.calling.api.OngoingCallMonitor

/**
 * State holder of the Account tab. The profile and statuses come from the local database; a status
 * change is applied optimistically and reverted (with a retry toast) when the server refuses it.
 */
class AccountViewModel internal constructor(
    private val repository: AccountRepository,
    /** Null on platforms without calling: logging out is then never blocked. */
    private val callMonitor: OngoingCallMonitor?,
) : ViewModel() {

    private val _state = MutableStateFlow(AccountState())
    val state: StateFlow<AccountState> = _state.asStateFlow()

    private val _effects = Channel<AccountEffect>(Channel.BUFFERED)
    val effects: Flow<AccountEffect> = _effects.receiveAsFlow()

    private var previousStatusId: String? = null
    private var lastChoiceId: String? = null

    init {
        repository.observe().onEach { snapshot ->
            if (snapshot == null) return@onEach
            _state.update { current ->
                current.copy(
                    profile = snapshot.agent.toProfile(),
                    statusGroups = snapshot.statuses.toGroups(),
                    // Keep the optimistic choice while the server call is in flight.
                    selectedStatusId = if (current.isChangingStatus) current.selectedStatusId else snapshot.currentStatusId,
                    currentPresence = snapshot.currentPresence(),
                    isLoading = false,
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onIntent(intent: AccountIntent) {
        when (intent) {
            is AccountIntent.SelectStatus -> changeStatus(intent.statusId, announce = true)
            AccountIntent.UndoStatusChange -> previousStatusId?.let { changeStatus(it, announce = false) }
            AccountIntent.RetryStatusChange -> lastChoiceId?.let { changeStatus(it, announce = true) }
            AccountIntent.LogOutClicked -> _state.update {
                it.copy(dialog = if (callMonitor?.hasActiveCall?.value == true) AccountDialog.EndCallFirst else AccountDialog.ConfirmLogOut)
            }
            AccountIntent.DismissDialog -> _state.update { it.copy(dialog = null) }
            AccountIntent.ConfirmLogOut -> {
                _state.update { it.copy(dialog = null) }
                _effects.trySend(AccountEffect.SignOut)
            }
            // No destination is designed for feedback yet.
            AccountIntent.SendFeedback -> Unit
        }
    }

    private fun changeStatus(statusId: String, announce: Boolean) {
        val current = _state.value
        val target = current.statusGroups.flatMap { it.statuses }.firstOrNull { it.id == statusId }
        if (target == null || target.isSystemSet || current.isChangingStatus || statusId == current.selectedStatusId) return
        previousStatusId = current.selectedStatusId
        lastChoiceId = statusId
        _state.update { it.copy(selectedStatusId = statusId, isChangingStatus = true) }
        viewModelScope.launch {
            val result = repository.changeStatus(statusId)
            // The database now holds the new status (success) or still the old one (failure).
            _state.update { it.copy(isChangingStatus = false, selectedStatusId = if (result.isSuccess) statusId else previousStatusId) }
            if (result.isSuccess) {
                if (announce) _effects.trySend(AccountEffect.StatusChanged(target))
            } else {
                _effects.trySend(AccountEffect.StatusChangeFailed)
            }
        }
    }
}
