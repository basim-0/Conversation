package org.talkdesk.conversation.calling.api

import kotlinx.coroutines.flow.StateFlow

/** Lets other features know whether the agent is in a call (e.g. Account blocks log out during one). */
interface OngoingCallMonitor {
    /** True from the moment a call rings or dials until it ends. */
    val hasActiveCall: StateFlow<Boolean>
}
