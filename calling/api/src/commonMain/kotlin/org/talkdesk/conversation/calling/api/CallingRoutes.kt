package org.talkdesk.conversation.calling.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** The dialer: number entry with the keypad. */
@Serializable
data object NewCallRoute : NavKey

/** The country list opened from the dialer to pick the dial code. The choice is remembered by the calling feature. */
@Serializable
data object CountryPickerRoute : NavKey
