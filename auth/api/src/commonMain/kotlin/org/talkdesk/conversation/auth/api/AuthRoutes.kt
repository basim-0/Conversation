package org.talkdesk.conversation.auth.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** Entry route of the auth feature. Which login screen shows is decided by the login ViewModel. */
@Serializable
data object LoginRoute : NavKey
