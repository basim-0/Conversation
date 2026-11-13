package org.talkdesk.conversation.auth.impl.domain

import org.talkdesk.conversation.auth.impl.domain.utils.AuthError

internal object AccountName {
    private val valid = Regex("^[a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?$")
    fun parse(raw: String): Result<String> {
        val name = raw.trim().lowercase()
        return when {
            name.isEmpty() -> Result.failure(AuthError.AccountEmpty)
            !valid.matches(name) -> Result.failure(AuthError.AccountInvalid)
            else -> Result.success(name)
        }
    }
}
