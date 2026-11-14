package org.talkdesk.conversation.account.impl.presentation

import org.talkdesk.conversation.account.impl.presentation.model.AgentProfile
import org.talkdesk.conversation.account.impl.presentation.model.AgentStatus
import org.talkdesk.conversation.account.impl.presentation.model.PresenceKind
import org.talkdesk.conversation.account.impl.presentation.model.StatusGroup
import org.talkdesk.conversation.account.impl.presentation.model.StatusGroupKind

/** TEMPORARY: the values of the design board. Replaced when the Account data layer is built. */
internal object AccountMock {
    const val AVAILABLE = "available"
    const val AFTER_CALL_WORK = "after-call-work"

    val profile = AgentProfile(name = "Alex Morgan", initials = "AM", subtitle = "Administrator · Support team")

    val statusGroups = listOf(
        StatusGroup(StatusGroupKind.Available, listOf(AgentStatus(AVAILABLE, "Available", PresenceKind.Available))),
        StatusGroup(
            StatusGroupKind.Away,
            listOf(
                AgentStatus("away", "Away", PresenceKind.Away),
                AgentStatus("away-lunch", "Away · Lunch", PresenceKind.Away),
                AgentStatus("away-meeting", "Away · Meeting", PresenceKind.Away),
                AgentStatus("away-training", "Away · Training", PresenceKind.Away),
            ),
        ),
        StatusGroup(StatusGroupKind.Offline, listOf(AgentStatus("offline", "Offline", PresenceKind.Offline))),
        StatusGroup(
            StatusGroupKind.Automatic,
            listOf(AgentStatus(AFTER_CALL_WORK, "After call work", PresenceKind.Busy, isSystemSet = true)),
        ),
    )
}
