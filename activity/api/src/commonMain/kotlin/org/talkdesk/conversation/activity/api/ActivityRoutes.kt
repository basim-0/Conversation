package org.talkdesk.conversation.activity.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** Calls and conversations in one timeline. Shown as the Activity tab of the home shell. */
@Serializable
data object ActivityRoute : NavKey
