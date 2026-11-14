package org.talkdesk.conversation.account.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** The agent's profile, presence status and log out. Shown as the Account tab of the home shell. */
@Serializable
data object AccountRoute : NavKey
