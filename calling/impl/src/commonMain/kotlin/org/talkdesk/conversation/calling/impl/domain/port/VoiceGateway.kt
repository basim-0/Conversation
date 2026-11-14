package org.talkdesk.conversation.calling.impl.domain.port

import org.talkdesk.conversation.calling.impl.domain.model.CallQuality
import org.talkdesk.conversation.calling.impl.domain.model.VoiceInvite

/**
 * The voice network (Twilio Voice): signalling and media. It knows nothing about Telecom, MVI or REST.
 * Calls are identified by an opaque handle chosen by the adapter. Callbacks arrive on any thread.
 *
 * Declared with listeners instead of `suspend`/`Flow` so that Swift can implement it (iOS Twilio SDK).
 */
interface VoiceGateway {
    fun setListener(listener: VoiceListener?)

    /** Binds [pushToken] to the agent's voice identity so the network can push call invites. */
    fun register(accessToken: String, pushToken: String, onResult: (error: String?) -> Unit)

    fun unregister(accessToken: String, pushToken: String, onResult: (error: String?) -> Unit)

    /** Starts an outgoing call and returns its handle at once; progress comes through [VoiceListener]. */
    fun connect(accessToken: String, parameters: Map<String, String>): String

    /** Answers the invite with this [handle]. Invites are single-use. */
    fun accept(handle: String)

    fun reject(handle: String)

    /** Ends the media leg of a connected or connecting call. */
    fun disconnect(handle: String)

    fun setMuted(handle: String, muted: Boolean)

    fun sendDigits(handle: String, digits: String)
}

interface VoiceListener {
    fun onInvite(invite: VoiceInvite)

    /** The invite is over before it was answered (caller hung up, another agent took it). */
    fun onInviteCancelled(handle: String)

    fun onRinging(handle: String, callSid: String?)
    fun onConnected(handle: String, callSid: String?)
    fun onConnectFailure(handle: String, error: String?)
    fun onReconnecting(handle: String)
    fun onReconnected(handle: String)

    /** [error] is null for a normal hang-up. */
    fun onDisconnected(handle: String, error: String?)
    fun onQualityChanged(handle: String, quality: CallQuality)
}
