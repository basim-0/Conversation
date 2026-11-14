package org.talkdesk.conversation.account.impl.ui

import org.talkdesk.conversation.account.impl.domain.model.StatusType
import org.talkdesk.conversation.account.impl.presentation.model.AgentProfile
import org.talkdesk.conversation.account.impl.presentation.model.AgentStatus
import org.talkdesk.conversation.account.impl.presentation.model.PresenceKind
import org.talkdesk.conversation.account.impl.presentation.model.StatusGroup
import org.talkdesk.conversation.account.impl.presentation.model.StatusGroupKind

/** Sample values for `@Preview`s only. Not used at runtime. */
internal object AccountPreviewData {
    const val AVAILABLE = "available"

    val profile = AgentProfile(name = "Alex Morgan", initials = "AM", subtitle = "Administrator")

    val statusGroups = listOf(
        StatusGroup(
            StatusGroupKind.Available,
            listOf(AgentStatus(AVAILABLE, null, StatusType.Available, PresenceKind.Available)),
        ),
        StatusGroup(
            StatusGroupKind.Away,
            listOf(
                AgentStatus("away", null, StatusType.Away, PresenceKind.Away),
                AgentStatus("away-lunch", "Away · Lunch", StatusType.Away, PresenceKind.Away),
                AgentStatus("away-meeting", "Away · Meeting", StatusType.Away, PresenceKind.Away),
            ),
        ),
        StatusGroup(
            StatusGroupKind.Offline,
            listOf(AgentStatus("offline", null, StatusType.Offline, PresenceKind.Offline)),
        ),
        StatusGroup(
            StatusGroupKind.Automatic,
            listOf(AgentStatus("after-call-work", null, StatusType.AfterCallWork, PresenceKind.Busy, isSystemSet = true)),
        ),
    )
}
