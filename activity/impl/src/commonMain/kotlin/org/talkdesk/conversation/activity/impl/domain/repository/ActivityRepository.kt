package org.talkdesk.conversation.activity.impl.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import org.talkdesk.conversation.activity.impl.domain.model.Activity
import org.talkdesk.conversation.activity.impl.domain.model.ActivityType

/** The local database is the source of truth: [observe] reads it, [refresh] and [loadMore] write into it. */
internal interface ActivityRepository {
    /** Cached activities of [types], newest first. */
    fun observe(types: Set<ActivityType>): Flow<List<Activity>>

    /** When the cache was last updated from the server, or null when it is empty. */
    fun observeLastSynced(): Flow<Instant?>

    /** Replaces the cache of [types] with the newest page. Returns whether more pages exist. */
    suspend fun refresh(types: Set<ActivityType>): Result<Boolean>

    /** Appends the next page of [types]. Returns whether more pages exist. */
    suspend fun loadMore(types: Set<ActivityType>): Result<Boolean>

    /** Drops everything cached for the signed-in agent. */
    suspend fun clear()
}
