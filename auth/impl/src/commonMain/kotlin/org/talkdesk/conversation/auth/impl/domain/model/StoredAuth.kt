package org.talkdesk.conversation.auth.impl.domain.model

internal data class StoredAuth(
    val accountName: String?,
    val regional: RegionalConfiguration?,
    val tokens: Tokens?,
    val refreshTokenExpired: Boolean,
)