package org.talkdesk.conversation.activity.impl.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** `GET interaction-contacts` page. */
@Serializable
internal data class InteractionContactsDto(
    val total: Int = 0,
    @SerialName("per_page") val perPage: Int = 0,
    val page: Int = 0,
    @SerialName("_embedded") val items: List<InteractionContactDto> = emptyList(),
)

@Serializable
internal data class InteractionContactDto(
    val id: String,
    val type: String,
    @SerialName("interaction_id") val interactionId: String? = null,
    @SerialName("user_name") val userName: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("contact_number") val contactNumber: String? = null,
    @SerialName("contact_person_id") val contactPersonId: String? = null,
    @SerialName("talk_time") val talkTime: Long? = null,
    @SerialName("started_at") val startedAt: String? = null,
    @SerialName("interaction_started_at") val interactionStartedAt: String? = null,
    @SerialName("external_integration") val externalIntegration: ExternalIntegrationDto? = null,
    val duration: Long? = null,
)

@Serializable
internal data class ExternalIntegrationDto(
    @SerialName("contact_person_channel_id") val contactPersonChannelId: String? = null,
)

/** `GET contacts` response. `_embedded` is absent when nothing matched. */
@Serializable
internal data class ContactsDto(
    @SerialName("_embedded") val embedded: ContactsEmbeddedDto? = null,
)

@Serializable
internal data class ContactsEmbeddedDto(val contacts: List<ContactDto> = emptyList())

@Serializable
internal data class ContactDto(
    val id: String,
    val name: String? = null,
    val phones: List<ContactPhoneDto>? = null,
    val emails: List<ContactEmailDto>? = null,
)

@Serializable
internal data class ContactPhoneDto(val number: String? = null)

@Serializable
internal data class ContactEmailDto(val email: String? = null)

