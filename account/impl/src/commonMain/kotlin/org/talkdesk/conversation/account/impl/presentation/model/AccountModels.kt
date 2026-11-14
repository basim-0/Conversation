package org.talkdesk.conversation.account.impl.presentation.model

import org.talkdesk.conversation.account.impl.domain.model.StatusType

/** Presence of an agent. Each kind is drawn with its own shape as well as colour. */
enum class PresenceKind {
    /** Filled disc. */
    Available,

    /** Thick ring. */
    Away,

    /** Thin ring. */
    Offline,

    /** Filled square. Used by statuses the system sets itself (After call work). */
    Busy,
}

/** Which group of the status list a status belongs to. The title of each group is localised by the UI. */
enum class StatusGroupKind { Available, Away, Offline, Automatic }

data class AgentProfile(
    val name: String,
    val initials: String,
    /** Role, e.g. "Administrator". */
    val subtitle: String,
)

data class AgentStatus(
    val id: String,
    /** Name defined by the account. Only set for custom statuses; others are named by the UI from [kind]. */
    val label: String?,
    val kind: StatusType,
    val presence: PresenceKind,
    /** Set by the system (After call work): shown disabled with an explanation, never selectable. */
    val isSystemSet: Boolean = false,
)

data class StatusGroup(val kind: StatusGroupKind, val statuses: List<AgentStatus>)
