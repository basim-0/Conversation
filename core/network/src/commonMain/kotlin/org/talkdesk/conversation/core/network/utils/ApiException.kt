package org.talkdesk.conversation.core.network.utils

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import org.talkdesk.conversation.core.network.NetworkJson

/** Error body returned by Talkdesk services: `{"code": "...", "message": "..."}`. */
data class ApiError(val code: String?, val message: String?)

/**
 * A failed network call: either an HTTP error ([httpStatus] + [error]) or a transport failure
 * (no [httpStatus], [isNetwork] is true).
 */
class ApiException(
    val httpStatus: Int?,
    val error: ApiError?,
    cause: Throwable? = null,
) : Exception("HTTP $httpStatus ${error?.code.orEmpty()} ${error?.message.orEmpty()}".trim(), cause) {
    val isNetwork: Boolean get() = httpStatus == null && cause is IOException
}

/** Parses [text] as a Talkdesk error body. Returns null when it is not one. */
fun parseApiError(text: String): ApiError? = runCatchingJson {
    val json: JsonObject = NetworkJson.parseToJsonElement(text).jsonObject
    val code = (json["code"] as? JsonPrimitive)?.contentOrNull
    val message = (json["message"] as? JsonPrimitive)?.contentOrNull
    if (code == null && message == null) null else ApiError(code, message)
}

/** Throws [ApiException] for a non-2xx response, otherwise returns this response. */
suspend fun HttpResponse.ensureSuccess(): HttpResponse {
    if (status.isSuccess()) return this
    throw ApiException(status.value, parseApiError(bodyAsText()))
}

/**
 * Runs [block] and converts transport failures into an [ApiException] with [ApiException.isNetwork].
 * Cancellation is always rethrown.
 */
suspend fun <T> apiCall(block: suspend () -> T): T = try {
    block()
} catch (e: ApiException) {
    throw e
} catch (e: CancellationException) {
    throw e
} catch (e: IOException) {
    throw ApiException(httpStatus = null, error = null, cause = e)
}

private inline fun <T> runCatchingJson(block: () -> T?): T? =
    try { block() } catch (_: SerializationException) { null } catch (_: IllegalArgumentException) { null }
