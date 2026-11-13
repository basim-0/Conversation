package org.talkdesk.conversation.auth.impl.domain.utils

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import org.talkdesk.conversation.auth.impl.secureRandomBytes
import org.talkdesk.conversation.auth.impl.sha256

internal class Pkce(val verifier: String, val challenge: String, val state: String) {
    companion object {
        fun generate(): Pkce {
            val verifier = base64Url(secureRandomBytes(32))
            val challenge = base64Url(sha256(verifier.encodeToByteArray()))
            return Pkce(verifier, challenge, state = base64Url(secureRandomBytes(16)))
        }
    }
}

@OptIn(ExperimentalEncodingApi::class)
internal fun base64Url(bytes: ByteArray): String =
    Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT).encode(bytes)
