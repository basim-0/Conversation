package org.talkdesk.conversation.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import org.talkdesk.conversation.core.database.entity.ActivityEntity

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activity WHERE type IN (:types) ORDER BY startedAtEpochMs DESC")
    fun observe(types: List<String>): Flow<List<ActivityEntity>>

    /** Time of the most recent successful sync, or null when nothing is cached. */
    @Query("SELECT MAX(syncedAtEpochMs) FROM activity")
    fun observeLastSynced(): Flow<Long?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(rows: List<ActivityEntity>)

    @Query("DELETE FROM activity WHERE type IN (:types)")
    suspend fun deleteByTypes(types: List<String>)

    /** Replaces everything cached for [types] with the freshly fetched first page. */
    @Transaction
    suspend fun replaceFirstPage(types: List<String>, rows: List<ActivityEntity>) {
        deleteByTypes(types)
        upsert(rows)
    }

    @Query("DELETE FROM activity WHERE startedAtEpochMs < :epochMs")
    suspend fun deleteOlderThan(epochMs: Long)

    @Query("DELETE FROM activity")
    suspend fun clear()
}