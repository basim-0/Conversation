package org.talkdesk.conversation.auth.impl

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.Security.SecRandomCopyBytes
import platform.Security.kSecRandomDefault

@OptIn(ExperimentalForeignApi::class)
internal actual fun secureRandomBytes(size: Int): ByteArray {
    val bytes = ByteArray(size)
    val status = bytes.usePinned { SecRandomCopyBytes(kSecRandomDefault, size.convert(), it.addressOf(0)) }
    check(status == 0) { "SecRandomCopyBytes failed: $status" }
    return bytes
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun sha256(input: ByteArray): ByteArray {
    require(input.isNotEmpty())
    val digest = UByteArray(CC_SHA256_DIGEST_LENGTH)
    input.usePinned { source ->
        digest.usePinned { target ->
            CC_SHA256(source.addressOf(0), input.size.convert(), target.addressOf(0))
        }
    }
    return digest.asByteArray()
}
