package org.talkdesk.conversation.core.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** Shared JSON configuration for every network call. */
val NetworkJson: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}

/**
 * Creates the base [HttpClient] (no authentication).
 *
 * @param engine override the platform engine (OkHttp / Darwin), mainly for tests.
 * @param userAgent value for the `User-Agent` header; omitted when null.
 * @param enableLogging enable HTTP logging. Must be true only in debug builds.
 */
fun createHttpClient(
    engine: HttpClientEngine? = null,
    userAgent: String? = null,
    enableLogging: Boolean = false,
): HttpClient {
    val configure: HttpClientConfig<*>.() -> Unit = {
        // Errors are mapped by `ensureSuccess()`, never thrown by Ktor.
        expectSuccess = false
        install(ContentNegotiation) { json(NetworkJson) }
        install(HttpTimeout) {
            connectTimeoutMillis = TIMEOUT_MILLIS
            requestTimeoutMillis = TIMEOUT_MILLIS
            socketTimeoutMillis = TIMEOUT_MILLIS
        }
        if (userAgent != null) {
            defaultRequest { header(HttpHeaders.UserAgent, userAgent) }
        }
        if (enableLogging) {
            install(Logging) {
                level = LogLevel.INFO
                sanitizeHeader { it == HttpHeaders.Authorization }
            }
        }
    }
    return if (engine != null) HttpClient(engine, configure) else HttpClient(configure)
}

private const val TIMEOUT_MILLIS = 30_000L
