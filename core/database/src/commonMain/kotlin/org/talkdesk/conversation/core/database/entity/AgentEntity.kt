package org.talkdesk.conversation.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** The signed-in agent profile (`agents/me`). Holds at most one row, [slot] is always [SLOT]. */
@Entity(tableName = "agent")
data class AgentEntity(
    @PrimaryKey val slot: Int = SLOT,
    val id: String,
    val accountId: String?,
    val name: String,
    val role: String?,
    /** Permission names joined with `,`. */
    val permissions: String,
    /** Id of the status the agent is in now, null until it is known. */
    val currentStatusId: String?,
    val syncedAtEpochMs: Long,
) {
    companion object {
        const val SLOT = 0
    }
}

/** A status the agent can be in (from `agents/me`), in server order. */
@Entity(tableName = "agent_status")
data class AgentStatusEntity(
    @PrimaryKey val id: String,
    /** Server label, shown for custom statuses. */
    val description: String?,
    val custom: Boolean,
    /** Server type: AVAILABLE, AWAY, BUSY, AFTER_CALL_WORK, OFFLINE or UNKNOWN. */
    val type: String,
    val readOnly: Boolean,
    val sortOrder: Int,
)

/** The outbound caller ID the agent currently uses. Holds at most one row. */
@Entity(tableName = "caller_id")
data class CallerIdEntity(
    @PrimaryKey val slot: Int = SLOT,
    val phoneNumber: String,
    val displayName: String?,
) {
    companion object {
        const val SLOT = 0
    }
}
