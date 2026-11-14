package org.talkdesk.conversation.account.impl.data.osiris

import com.talkdesk.mobile.core.error.ServiceException
import com.talkdesk.mobile.core.http.HttpHeaders
import com.talkdesk.mobile.core.http.HttpPostComponents
import com.talkdesk.mobile.core.http.RequestExecutor
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.talkdesk.conversation.core.network.utils.ensureSuccess

/**
 * Lets the Osiris SDK, which makes blocking calls, use the app's authorized Ktor client so it gets the
 * bearer header and the token refresh. Osiris calls this from its own background threads.
 */
internal class KtorRequestExecutor(private val client: HttpClient) : RequestExecutor {

    override fun get(url: String, headers: HttpHeaders): String =
        execute { client.get(url) { applyHeaders(headers) } }

    override fun post(url: String, headers: HttpHeaders, urlComponents: HttpPostComponents): String {
        // Values are already URL-encoded by the caller: send them as they are.
        val form = urlComponents.components.entries.joinToString("&") { "${it.key}=${it.value}" }
        return execute {
            client.post(url) {
                applyHeaders(headers)
                setBody(TextContent(form, ContentType.Application.FormUrlEncoded))
            }
        }
    }

    override fun post(url: String, jsonBody: String, headers: HttpHeaders): String = execute {
        client.post(url) {
            applyHeaders(headers)
            setBody(TextContent(jsonBody, ContentType.Application.Json))
        }
    }

    override fun patch(url: String, jsonBody: String, headers: HttpHeaders): String = execute {
        client.patch(url) {
            applyHeaders(headers)
            setBody(TextContent(jsonBody, ContentType.Application.Json))
        }
    }

    private fun execute(request: suspend () -> HttpResponse): String = try {
        runBlocking(Dispatchers.IO) { request().ensureSuccess().bodyAsText() }
    } catch (e: ServiceException) {
        throw e
    } catch (e: Exception) {
        throw ServiceException(e)
    }

    private fun HttpRequestBuilder.applyHeaders(headers: HttpHeaders) {
        headers.headers.forEach { (name, value) ->
            // The body decides these two.
            if (!name.equals("Content-Type", true) && !name.equals("Content-Length", true)) header(name, value)
        }
    }
}
