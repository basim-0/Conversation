package org.talkdesk.conversation.auth.impl.domain.utils

import kotlinx.coroutines.CancellationException
import org.talkdesk.conversation.core.network.utils.ApiException

/** Typed failures of the auth feature. User-facing text is produced by each platform UI. */
sealed class AuthError(cause: Throwable? = null) : Exception(cause) {
    /** The account name is blank. */
    data object AccountEmpty : AuthError()

    /** The account name has characters that are not allowed. */
    data object AccountInvalid : AuthError()

    /** No Talkdesk account with that name. */
    data object AccountNotFound : AuthError()

    data class Offline(override val cause: Throwable?) : AuthError(cause)

    /** The redirect did not match the request that was started (or none was started). */
    data object StateMismatch : AuthError()

    data class SignInFailed(override val cause: Throwable? = null) : AuthError(cause)

    data object NotWhitelisted : AuthError()

    data object MissingScopes : AuthError()
}

/**
 * Runs [block] and returns its result, mapping network failures to [AuthError]. An [AuthError]
 * thrown by [block] is kept as is. Cancellation is always rethrown.
 */
internal inline fun <T> authCatching(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: AuthError) {
    Result.failure(e)
} catch (e: ApiException) {
    Result.failure(if (e.isNetwork) AuthError.Offline(e) else AuthError.SignInFailed(e))
} catch (e: Exception) {
    Result.failure(AuthError.SignInFailed(e))
}
