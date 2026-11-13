package org.talkdesk.conversation.auth.impl.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.http.Parameters
import io.ktor.http.parameters
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.talkdesk.conversation.auth.impl.AuthConfig
import org.talkdesk.conversation.auth.impl.data.ClientAssertionFactory
import org.talkdesk.conversation.auth.impl.domain.model.OAuthCredentials
import org.talkdesk.conversation.auth.impl.domain.model.Region
import org.talkdesk.conversation.auth.impl.domain.model.RegionalConfiguration
import org.talkdesk.conversation.core.network.utils.apiCall
import org.talkdesk.conversation.core.network.utils.ensureSuccess

@Serializable
internal data class EntrypointDto(
    @SerialName("target_issuer") val targetIssuer: String,
    @SerialName("td_region") val tdRegion: String? = null,
    @SerialName("target_gateway") val targetGateway: String,
)

@Serializable
internal data class TokenDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String? = null,
    val scope: String? = null,
)

/** Unauthenticated Talkdesk ID calls. Uses the base client (no bearer header). */
internal class TalkdeskIdApi(
    private val client: HttpClient,
    private val assertions: ClientAssertionFactory,
) {
    suspend fun lookUpRegion(accountName: String): RegionalConfiguration = apiCall {
        val url = "${AuthConfig.PROTOCOL}${AuthConfig.ENTRYPOINT_HOST}/accounts/$accountName/entrypoint"
        val dto = client.get(url).ensureSuccess().body<EntrypointDto>()
        RegionalConfiguration(dto.targetIssuer, Region.fromTdRegion(dto.tdRegion), dto.targetGateway)
    }

    suspend fun exchangeCode(
        regional: RegionalConfiguration,
        credentials: OAuthCredentials,
        code: String,
        codeVerifier: String,
    ): TokenDto = tokenCall(regional, credentials) { assertion ->
        parameters {
            append("grant_type", "authorization_code")
            append("code", code)
            append("redirect_uri", AuthConfig.REDIRECT_URI)
            append("code_verifier", codeVerifier)
            append("client_id", credentials.clientId)
            append("client_assertion_type", ASSERTION_TYPE)
            append("client_assertion", assertion)
        }
    }

    suspend fun refresh(
        regional: RegionalConfiguration,
        credentials: OAuthCredentials,
        refreshToken: String,
        scope: String,
    ): TokenDto = tokenCall(regional, credentials) { assertion ->
        parameters {
            append("grant_type", "refresh_token")
            append("refresh_token", refreshToken)
            append("scope", scope)
            append("client_assertion_type", ASSERTION_TYPE)
            append("client_assertion", assertion)
        }
    }

    suspend fun endSession(regional: RegionalConfiguration, credentials: OAuthCredentials, refreshToken: String) {
        apiCall {
            val url = regional.url(AuthConfig.END_SESSION_PATH)
            client.submitForm(
                url = url,
                formParameters = parameters {
                    append("client_id", credentials.clientId)
                    append("refresh_token", refreshToken)
                    append("client_assertion_type", ASSERTION_TYPE)
                    append("client_assertion", assertions.create(credentials, audience = url))
                },
            ).ensureSuccess()
        }
    }

    private suspend fun tokenCall(
        regional: RegionalConfiguration,
        credentials: OAuthCredentials,
        form: (assertion: String) -> Parameters,
    ): TokenDto = apiCall {
        val url = regional.url(AuthConfig.TOKEN_PATH)
        client.submitForm(url, form(assertions.create(credentials, audience = url)))
            .ensureSuccess()
            .body<TokenDto>()
    }

    private companion object {
        const val ASSERTION_TYPE = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer"
    }
}
