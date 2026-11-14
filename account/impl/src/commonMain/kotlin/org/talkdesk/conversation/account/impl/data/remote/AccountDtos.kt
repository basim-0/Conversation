package org.talkdesk.conversation.account.impl.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class AgentMeDto(
    val id: String,
    @SerialName("account_id") val accountId: String? = null,
    val name: String = "",
    val role: String? = null,
    val statuses: List<StatusDto> = emptyList(),
    val permissions: List<String> = emptyList(),
)

@Serializable
internal data class StatusDto(
    val id: String,
    val description: String? = null,
    val custom: Boolean = false,
    val type: String? = null,
    @SerialName("read_only") val readOnly: Boolean = false,
)

@Serializable
internal data class UserStatusesRequest(@SerialName("user_ids") val userIds: List<String>)

@Serializable
internal data class UserStatusDto(val id: String, val status: String)

@Serializable
internal data class SetStatusRequest(val status: String, val reason: String)

@Serializable
internal data class HeartbeatRequest(val duration: Long)
