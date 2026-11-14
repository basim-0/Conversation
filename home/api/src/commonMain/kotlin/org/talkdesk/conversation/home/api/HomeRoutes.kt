package org.talkdesk.conversation.home.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** The signed-in home: bottom navigation with Activity, Contacts, Conversations, Voicemails and Account. */
@Serializable
data object HomeRoute : NavKey

// Tab destinations whose feature does not exist yet. Each moves to its own feature's `api` module
// (like `ActivityRoute` in `:activity:api`) when that feature is built; until then Home shows a placeholder.

@Serializable
data object ContactsRoute : NavKey

@Serializable
data object ConversationsRoute : NavKey

@Serializable
data object VoicemailsRoute : NavKey
