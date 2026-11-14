package org.talkdesk.conversation.calling.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** The dialer: number entry with the keypad. */
@Serializable
data object NewCallRoute : NavKey

/** The call screen: calling, on call and on hold, with the in-call keypad. */
@Serializable
data object CallRoute : NavKey

/** Full-screen incoming call (lock screen / full-screen intent). */
@Serializable
data object IncomingCallRoute : NavKey
