package org.talkdesk.conversation.account.impl.domain.model

import org.talkdesk.conversation.account.api.Agent

/** Server type of a status. */
enum class StatusType {
    Available, Away, Busy, AfterCallWork, Offline, Unknown;

    companion object {
        fun fromServer(type: String?): StatusType = when (type?.uppercase()) {
            "AVAILABLE" -> Available
            "AWAY" -> Away
            "BUSY" -> Busy
            "AFTER_CALL_WORK" -> AfterCallWork
            "OFFLINE" -> Offline
            else -> Unknown
        }
    }
}

internal data class StatusInfo(
    val id: String,
    /** Server label; only meaningful for custom statuses. */
    val description: String?,
    val custom: Boolean,
    val type: StatusType,
    val readOnly: Boolean,
)

/** The cached profile with the statuses the agent can pick and the one it is in now. */
internal data class AccountSnapshot(
    val agent: Agent,
    val statuses: List<StatusInfo>,
    val currentStatusId: String?,
)
