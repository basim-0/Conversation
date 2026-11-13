package org.talkdesk.conversation.activity.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import org.talkdesk.conversation.activity.impl.domain.model.Activity
import org.talkdesk.conversation.activity.impl.domain.repository.ActivityRepository
import org.talkdesk.conversation.activity.impl.domain.utils.ActivityError
import kotlin.time.Clock

/**
 * State holder of the Activity tab. Rows come only from the local database ([ActivityRepository.observe]);
 * refresh and load-more write into it. Changing the tab or channels shows the cache of that selection
 * straight away and fetches its first page.
 */
class ActivityViewModel internal constructor(
    private val repository: ActivityRepository,
    private val clock: Clock = Clock.System,
) : ViewModel() {

    private val _state = MutableStateFlow(ActivityState())
    val state: StateFlow<ActivityState> = _state.asStateFlow()

    /** Whether the first fetch of a selection finished, so empty rows mean "empty" and not "loading". */
    private val syncPhases = MutableStateFlow<Map<Selection, SyncPhase>>(emptyMap())
    private var fetchJob: Job? = null

    private val selection: Flow<Selection> = _state
        .map { Selection(it.selectedTab, it.selectedChannels) }
        .distinctUntilChanged()

    init {
        viewModelScope.launch {
            selection.flatMapLatest { sel ->
                val phase = syncPhases.map { it[sel] ?: SyncPhase.Loading }.distinctUntilChanged()
                combine(repository.observe(sel.types), phase) { rows, p -> toContent(rows, p) }
            }.collect { content -> _state.update { it.copy(content = content) } }
        }
        viewModelScope.launch {
            repository.observeLastSynced().collect { synced ->
                val label = synced?.toUpdatedLabel(clock.now(), TimeZone.currentSystemDefault())
                _state.update { it.copy(lastUpdatedLabel = label) }
            }
        }
        viewModelScope.launch { selection.collect { sel -> fetchFirstPage(sel, userInitiated = false) } }
    }

    fun onIntent(intent: ActivityIntent) {
        when (intent) {
            is ActivityIntent.SelectTab -> _state.update { it.copy(selectedTab = intent.tab) }
            is ActivityIntent.ToggleChannel -> _state.update {
                val channels = if (intent.channel in it.selectedChannels) {
                    it.selectedChannels - intent.channel
                } else {
                    it.selectedChannels + intent.channel
                }
                it.copy(selectedChannels = channels)
            }
            ActivityIntent.ClearFilters -> _state.update { it.copy(selectedChannels = emptySet()) }
            ActivityIntent.Refresh -> fetchFirstPage(_state.value.selection(), userInitiated = true)
            ActivityIntent.Retry -> {
                val sel = _state.value.selection()
                syncPhases.update { it - sel }
                fetchFirstPage(sel, userInitiated = false)
            }
            ActivityIntent.LoadMore -> loadMore()
            // Calling and the details page are separate features that do not exist yet.
            is ActivityIntent.CallBack, is ActivityIntent.OpenItem, ActivityIntent.NewCall -> Unit
        }
    }

    private fun fetchFirstPage(sel: Selection, userInitiated: Boolean) {
        fetchJob?.cancel()
        _state.update { it.copy(isRefreshing = userInitiated, isLoadingMore = false, canLoadMore = false) }
        fetchJob = viewModelScope.launch {
            repository.refresh(sel.types).fold(
                onSuccess = { hasMore ->
                    syncPhases.update { it + (sel to SyncPhase.Synced) }
                    _state.update { it.copy(isOffline = false, canLoadMore = hasMore) }
                },
                onFailure = { error ->
                    // Cached rows stay; the screen shows Failed only when there are none.
                    syncPhases.update { phases ->
                        if (phases[sel] == SyncPhase.Synced) phases else phases + (sel to SyncPhase.Failed)
                    }
                    _state.update { it.copy(isOffline = error is ActivityError.Offline) }
                },
            )
            _state.update { it.copy(isRefreshing = false) }
        }
    }

    private fun loadMore() {
        val current = _state.value
        if (!current.canLoadMore || current.isLoadingMore || current.isRefreshing) return
        val sel = current.selection()
        fetchJob = viewModelScope.launch {
            _state.update { it.copy(isLoadingMore = true) }
            repository.loadMore(sel.types).fold(
                onSuccess = { hasMore -> _state.update { it.copy(canLoadMore = hasMore, isOffline = false) } },
                onFailure = { error -> _state.update { it.copy(isOffline = error is ActivityError.Offline) } },
            )
            _state.update { it.copy(isLoadingMore = false) }
        }
    }

    private fun toContent(rows: List<Activity>, phase: SyncPhase): ActivityContent = when {
        rows.isNotEmpty() -> ActivityContent.Loaded(rows.toSections(clock.now(), TimeZone.currentSystemDefault()))
        phase == SyncPhase.Synced -> ActivityContent.Loaded(emptyList())
        phase == SyncPhase.Failed -> ActivityContent.Failed
        else -> ActivityContent.Loading
    }

    private fun ActivityState.selection() = Selection(selectedTab, selectedChannels)

    private data class Selection(val tab: ActivityTab, val channels: Set<ActivityChannel>) {
        val types get() = typesFor(tab, channels)
    }

    private enum class SyncPhase { Loading, Synced, Failed }
}
