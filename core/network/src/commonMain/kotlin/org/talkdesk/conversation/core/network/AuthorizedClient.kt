package org.talkdesk.conversation.core.network

import org.koin.core.qualifier.named

/**
 * Qualifier of the authenticated `HttpClient` (bearer header + refresh on expiry). Provided by the auth
 * feature; other features inject it with `get<HttpClient>(AuthorizedHttpClient)`.
 */
val AuthorizedHttpClient = named("network.authorizedClient")

/**
 * Base URL of the signed-in account's regional gateway, e.g. `https://api.talkdeskapp.com`
 * (no trailing slash). Provided by the auth feature, which owns the regional configuration.
 */
fun interface ApiBaseUrl {
    /** Throws when there is no signed-in account. */
    suspend fun get(): String
}
