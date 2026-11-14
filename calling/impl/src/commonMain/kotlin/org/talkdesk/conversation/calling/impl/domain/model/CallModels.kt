package org.talkdesk.conversation.calling.impl.domain.model

enum class CallDirection { Incoming, Outgoing }

/** Why a call ended. Platforms map it to their own cause (Telecom `DisconnectCause`, CallKit end reason). */
enum class EndReason {
    /** The agent ended it. */
    Local,

    /** The other side ended it. */
    Remote,

    /** It rang and nobody answered (the caller gave up or another agent took it). */
    Missed,

    /** The agent declined it. */
    Rejected,

    /** It could not be set up or broke. */
    Failed,
}

enum class CallQuality { Excellent, Good, Poor }

enum class AudioEndpointType { Earpiece, Speaker, Bluetooth, Wired, Other }

/** An audio output the call can use. [id] is opaque and only meaningful to the platform that issued it. */
data class AudioEndpoint(val id: String, val type: AudioEndpointType, val name: String)

/** A call offered by the voice network. [handle] identifies it to the [org.talkdesk.conversation.calling.impl.domain.port.VoiceGateway]. */
data class VoiceInvite(
    val handle: String,
    /** Twilio call sid. */
    val callSid: String,
    val from: String?,
    /** Custom parameters set by Talkdesk: `customer_phone_number`, `from_agent_id`, `call_sid`, `transfer_type`. */
    val parameters: Map<String, String>,
)

/** Where audio can go now and where it goes. */
data class AudioRoutes(val available: List<AudioEndpoint> = emptyList(), val current: AudioEndpoint? = null)
