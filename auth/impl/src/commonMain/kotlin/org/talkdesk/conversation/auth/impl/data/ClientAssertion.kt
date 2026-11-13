package org.talkdesk.conversation.auth.impl.data

import kotlin.time.Clock
import kotlin.uuid.Uuid
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.talkdesk.conversation.auth.impl.AuthConfig
import org.talkdesk.conversation.auth.impl.domain.model.OAuthCredentials
import org.talkdesk.conversation.auth.impl.domain.utils.base64Url

/**
 * ES256 signing primitive. Returns the JOSE signature (raw `R || S`, 64 bytes), not DER.
 * Implemented per platform (Java security on Android, CryptoKit in Swift on iOS). Keeping signing
 * behind this interface lets the embedded-key approach be swapped after the security review.
 */
interface Es256Signer {
    fun sign(privateKeyPkcs8Base64: String, payload: ByteArray): ByteArray
}

/** Builds the signed `private_key_jwt` client assertion used on token and end-session calls. */
internal class ClientAssertionFactory(
    private val signer: Es256Signer,
    private val nowSeconds: () -> Long = { Clock.System.now().epochSeconds },
) {
    fun create(credentials: OAuthCredentials, audience: String): String {
        val now = nowSeconds()
        val header = buildJsonObject {
            put("alg", "ES256")
            put("kid", credentials.keyId)
        }
        val claims = buildJsonObject {
            put("jti", Uuid.random().toString())
            put("iss", credentials.clientId)
            put("sub", credentials.clientId)
            put("aud", audience)
            put("iat", now)
            put("exp", now + AuthConfig.ASSERTION_LIFETIME_SECONDS)
        }
        val signingInput = base64Url(header.toString().encodeToByteArray()) + "." +
            base64Url(claims.toString().encodeToByteArray())
        val signature = signer.sign(credentials.privateKey, signingInput.encodeToByteArray())
        return signingInput + "." + base64Url(signature)
    }
}
