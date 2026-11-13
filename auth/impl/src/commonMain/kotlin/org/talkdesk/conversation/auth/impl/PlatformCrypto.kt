package org.talkdesk.conversation.auth.impl

/** Cryptographically secure random bytes. */
internal expect fun secureRandomBytes(size: Int): ByteArray

internal expect fun sha256(input: ByteArray): ByteArray
