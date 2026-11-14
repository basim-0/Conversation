package org.talkdesk.conversation.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import org.talkdesk.conversation.core.database.entity.AgentEntity
import org.talkdesk.conversation.core.database.entity.AgentStatusEntity

@Dao
interface AgentDao {
    @Query("SELECT * FROM agent LIMIT 1")
    fun observeAgent(): Flow<AgentEntity?>

    @Query("SELECT * FROM agent LIMIT 1")
    suspend fun agent(): AgentEntity?

    @Query("SELECT * FROM agent_status ORDER BY sortOrder")
    fun observeStatuses(): Flow<List<AgentStatusEntity>>

    @Query("SELECT * FROM agent_status ORDER BY sortOrder")
    suspend fun statuses(): List<AgentStatusEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAgent(agent: AgentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStatuses(rows: List<AgentStatusEntity>)

    @Query("DELETE FROM agent_status")
    suspend fun clearStatuses()

    @Query("UPDATE agent SET currentStatusId = :statusId")
    suspend fun setCurrentStatus(statusId: String?)

    @Query("DELETE FROM agent")
    suspend fun clearAgent()

    /** Replaces the cached profile while keeping the known current status when the server did not send one. */
    @Transaction
    suspend fun replaceProfile(agent: AgentEntity, statuses: List<AgentStatusEntity>) {
        val keepStatus = agent.currentStatusId ?: agent()?.takeIf { it.id == agent.id }?.currentStatusId
        upsertAgent(agent.copy(currentStatusId = keepStatus))
        clearStatuses()
        upsertStatuses(statuses)
    }

    @Transaction
    suspend fun clear() {
        clearAgent()
        clearStatuses()
    }
}
