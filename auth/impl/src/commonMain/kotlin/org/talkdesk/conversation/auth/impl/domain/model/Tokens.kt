package org.talkdesk.conversation.auth.impl.domain.model

internal data class Tokens(
    val accessToken: String,
    val refreshToken: String,
    val scope: String,
    val updatedAtMillis: Long,
)