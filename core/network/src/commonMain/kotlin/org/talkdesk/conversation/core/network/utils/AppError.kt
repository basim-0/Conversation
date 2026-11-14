package org.talkdesk.conversation.core.network.utils

import kotlinx.coroutines.CancellationException

/**
 * Typed error shared by features. Holds no user text: each platform maps it to its own strings
 * (Android string resources, iOS String Catalog).
 */
sealed class AppError {
    /** No connectivity or the request never reached the server. */
    data object Offline : AppError()

    /** The session is not valid any more (HTTP 401). */
    data object Unauthorized : AppError()

    /** The server answered with an error ([httpStatus] and the Talkdesk error [code], when present). */
    data class Server(val httpStatus: Int, val code: String?) : AppError()

    /** Anything else. */
    data class Unknown(val cause: Throwable?) : AppError()
}

/** A [Throwable] carrying an [AppError], so it can travel inside a [Result]. */
class AppException(val error: AppError, cause: Throwable? = null) : Exception(error.toString(), cause)

/** The [AppError] carried by this failure, or [AppError.Unknown] for any other throwable. */
fun Throwable.toAppError(): AppError = when (this) {
    is AppException -> error
    is ApiException -> when {
        isNetwork -> AppError.Offline
        httpStatus == 401 -> AppError.Unauthorized
        httpStatus != null -> AppError.Server(httpStatus, error?.code)
        else -> AppError.Unknown(this)
    }
    else -> AppError.Unknown(this)
}

/** The [AppError] of a failed [Result], or null when it succeeded. */
fun Result<*>.appErrorOrNull(): AppError? = exceptionOrNull()?.toAppError()

/**
 * Data layer: runs a network/disk [block] and returns a [Result] whose failure is an [AppException].
 * Use instead of `runCatching`: cancellation is always rethrown.
 */
suspend fun <T> dataCatching(block: suspend () -> T): Result<T> = try {
    Result.success(apiCall(block))
} catch (e: CancellationException) {
    throw e
} catch (e: Throwable) {
    Result.failure(AppException(e.toAppError(), e))
}

/**
 * Domain/presentation: runs [block] and returns a [Result]. Use instead of `runCatching`:
 * cancellation is always rethrown and failures are normalised to an [AppException].
 */
inline fun <T> resultCatching(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Throwable) {
    Result.failure(AppException(e.toAppError(), e))
}
