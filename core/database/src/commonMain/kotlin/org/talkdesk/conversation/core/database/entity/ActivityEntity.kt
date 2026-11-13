package org.talkdesk.conversation.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** One cached Activity row (an interaction contact: call, SMS, WhatsApp or email). */
@Entity(tableName = "activity", indices = [Index("type"), Index("startedAtEpochMs")])
data class ActivityEntity(
    @PrimaryKey val id: String,
    /** Server type name, e.g. `MISSED_INBOUND`. */
    val type: String,
    val contactName: String?,
    val contactNumber: String?,
    val agentName: String?,
    /** The row belongs to the signed-in agent. */
    val isMine: Boolean,
    val startedAtEpochMs: Long,
    val durationSeconds: Long,
    /** When this row was last written from the server. */
    val syncedAtEpochMs: Long,
)