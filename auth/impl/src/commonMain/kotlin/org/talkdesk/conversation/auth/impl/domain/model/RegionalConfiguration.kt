package org.talkdesk.conversation.auth.impl.domain.model

import org.talkdesk.conversation.auth.impl.AuthConfig

internal data class RegionalConfiguration(val issuer: String, val region: Region, val gateway: String) {
    fun url(path: String) = "${AuthConfig.PROTOCOL}$issuer$path"
}

class OAuthCredentials(val clientId: String, val privateKey: String, val keyId: String) {
    override fun toString(): String = "OAuthCredentials(clientId=$clientId)"
}