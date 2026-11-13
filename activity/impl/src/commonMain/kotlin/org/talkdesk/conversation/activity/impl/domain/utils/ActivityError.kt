package org.talkdesk.conversation.activity.impl.domain.utils

import kotlinx.coroutines.CancellationException
import org.talkdesk.conversation.core.network.utils.ApiException

/** Typed failures of the activity feature. User-facing text is produced by each platform UI. */
internal sealed class ActivityError(cause: Throwable?) : Exception(cause) {
    /** No connection to the server. */
    class Offline(cause: Throwable?) : ActivityError(cause)

    class Failed(cause: Throwable?) : ActivityError(cause)
}

/** Runs [block] and returns its result, mapping failures to [ActivityError]. Cancellation is always rethrown. */
internal inline fun <T> activityCatching(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: ApiException) {
    Result.failure(if (e.isNetwork) ActivityError.Offline(e) else ActivityError.Failed(e))
} catch (e: Exception) {
    Result.failure(ActivityError.Failed(e))
}
