package org.talkdesk.conversation.calling.impl.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class VoiceTokenDto(val credentials: VoiceCredentialsDto)

@Serializable
internal data class VoiceCredentialsDto(@SerialName("access_token") val accessToken: String)

/** `calls-info`: what Talkdesk knows about a call (contact, line, interaction). */
@Serializable
internal data class CallInfoDto(
    /** The interaction id. */
    val id: String? = null,
    val status: String? = null,
    @SerialName("phone_number") val phoneNumber: String? = null,
    @SerialName("ring_groups") val ringGroups: List<String> = emptyList(),
    val contact: CallContactDto? = null,
    /** Agent-to-agent calls only. */
    @SerialName("contact_agent_name") val contactAgentName: String? = null,
) {
    val isAnswered: Boolean get() = status.equals("ANSWERED", ignoreCase = true)
}

@Serializable
internal data class CallContactDto(
    val name: String? = null,
    val phones: List<CallContactPhoneDto> = emptyList(),
)

@Serializable
internal data class CallContactPhoneDto(val number: String? = null, val label: String? = null)

@Serializable
internal data class HoldRequest(@SerialName("should_play_hold_beep") val shouldPlayHoldBeep: Boolean)

@Serializable
internal data class DisconnectRequest(@SerialName("interaction_id") val interactionId: String? = null)

@Serializable
internal data class CallerIdsDto(@SerialName("selected_caller_id") val selected: CallerIdDto? = null)

@Serializable
internal data class CallerIdDto(
    @SerialName("phone_number") val phoneNumber: String,
    @SerialName("display_name") val displayName: String? = null,
)
