package org.talkdesk.conversation.account.impl.presentation

import org.talkdesk.conversation.account.api.Agent
import org.talkdesk.conversation.account.impl.domain.model.AccountSnapshot
import org.talkdesk.conversation.account.impl.domain.model.StatusInfo
import org.talkdesk.conversation.account.impl.domain.model.StatusType
import org.talkdesk.conversation.account.impl.presentation.model.AgentProfile
import org.talkdesk.conversation.account.impl.presentation.model.AgentStatus
import org.talkdesk.conversation.account.impl.presentation.model.PresenceKind
import org.talkdesk.conversation.account.impl.presentation.model.StatusGroup
import org.talkdesk.conversation.account.impl.presentation.model.StatusGroupKind

/** Ids the legacy app never offers for manual selection. */
private val HiddenStatusIds = setOf("busy", "unknown")

internal fun Agent.toProfile() = AgentProfile(
    name = name,
    initials = name.split(' ').filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() },
    subtitle = role.orEmpty().replace('_', ' ').replaceFirstChar { it.uppercase() },
)

private fun StatusType.presence() = when (this) {
    StatusType.Available -> PresenceKind.Available
    StatusType.Away -> PresenceKind.Away
    StatusType.Offline, StatusType.Unknown -> PresenceKind.Offline
    StatusType.Busy, StatusType.AfterCallWork -> PresenceKind.Busy
}

private fun StatusInfo.toStatus() = AgentStatus(
    id = id,
    label = description?.takeIf { custom && it.isNotBlank() },
    kind = type,
    presence = type.presence(),
    isSystemSet = type == StatusType.AfterCallWork || readOnly,
)

private fun StatusInfo.group() = when (type) {
    StatusType.Available -> StatusGroupKind.Available
    StatusType.Away -> StatusGroupKind.Away
    StatusType.Offline -> StatusGroupKind.Offline
    StatusType.Busy, StatusType.AfterCallWork -> StatusGroupKind.Automatic
    StatusType.Unknown -> null
}

/** Selectable statuses grouped by meaning, in the account's order. Empty groups are dropped. */
internal fun List<StatusInfo>.toGroups(): List<StatusGroup> {
    val visible = filter { it.id !in HiddenStatusIds && it.group() != null }
    return StatusGroupKind.entries.mapNotNull { kind ->
        val inGroup = visible.filter { it.group() == kind }
        if (inGroup.isEmpty()) null else StatusGroup(kind, inGroup.map { it.toStatus() })
    }
}

internal fun AccountSnapshot.currentPresence(): PresenceKind? =
    statuses.firstOrNull { it.id == currentStatusId }?.type?.presence()
