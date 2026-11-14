package org.talkdesk.conversation.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.talkdesk.conversation.core.database.entity.CallerIdEntity

@Dao
interface CallerIdDao {
    @Query("SELECT * FROM caller_id LIMIT 1")
    fun observe(): Flow<CallerIdEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(row: CallerIdEntity)

    @Query("DELETE FROM caller_id")
    suspend fun clear()
}
