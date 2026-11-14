package org.talkdesk.conversation.core.push

import kotlinx.coroutines.flow.Flow

/** A push message received by the device: the data payload only. */
class PushMessage(val data: Map<String, String>)

/**
 * Reacts to push messages. Every feature that owns a push type (calls, voicemail, ACW...) registers one
 * in Koin; each handler ignores messages that are not its own.
 *
 * Handlers run on the thread that delivers the push and must return quickly: start work asynchronously.
 */
fun interface PushHandler {
    fun handle(message: PushMessage)
}

/** The push address of this device. */
interface PushTokenProvider {
    /** The current token, or null when it cannot be obtained now (no Play services, offline...). */
    suspend fun token(): String?

    /** Emits every new token after the platform rotates it. */
    val tokenChanges: Flow<String>
}

/**
 * Identifies the push service for Talkdesk's device registration.
 * Android: `firebase`, no bundle. iOS: `apns` plus the app bundle id.
 */
class PushPlatform(val serviceName: String, val bundle: String? = null)
