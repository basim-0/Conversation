package org.talkdesk.conversation.auth.impl

import java.security.KeyFactory
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import org.talkdesk.conversation.auth.impl.data.Es256Signer

/** ES256 signing with the JDK/Android security provider (no extra crypto library). */
class JavaEs256Signer : Es256Signer {
    @OptIn(ExperimentalEncodingApi::class)
    override fun sign(privateKeyPkcs8Base64: String, payload: ByteArray): ByteArray {
        val keyBytes = Base64.Mime.decode(privateKeyPkcs8Base64)
        val key = KeyFactory.getInstance("EC").generatePrivate(PKCS8EncodedKeySpec(keyBytes))
        val der = Signature.getInstance("SHA256withECDSA").run {
            initSign(key)
            update(payload)
            sign()
        }
        return derToJose(der)
    }
}

/** Converts a DER `SEQUENCE { INTEGER r, INTEGER s }` signature into the fixed-size `R || S` form. */
internal fun derToJose(der: ByteArray, size: Int = 32): ByteArray {
    var offset = 2
    if (der[1].toInt() and 0x80 != 0) offset += der[1].toInt() and 0x7f
    require(der[offset].toInt() == 0x02) { "Invalid DER signature" }
    val rLength = der[offset + 1].toInt() and 0xff
    val r = der.copyOfRange(offset + 2, offset + 2 + rLength)
    offset += 2 + rLength
    require(der[offset].toInt() == 0x02) { "Invalid DER signature" }
    val sLength = der[offset + 1].toInt() and 0xff
    val s = der.copyOfRange(offset + 2, offset + 2 + sLength)

    val out = ByteArray(size * 2)
    fun ByteArray.copyFixed(target: Int) {
        val trimmed = dropWhile { it == 0.toByte() }.toByteArray()
        require(trimmed.size <= size) { "Invalid DER signature" }
        trimmed.copyInto(out, target + size - trimmed.size)
    }
    r.copyFixed(0)
    s.copyFixed(size)
    return out
}
