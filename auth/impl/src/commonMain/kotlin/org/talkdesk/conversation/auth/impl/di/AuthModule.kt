package org.talkdesk.conversation.auth.impl.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.talkdesk.conversation.auth.api.AuthSession
import org.talkdesk.conversation.auth.impl.data.repository.AgentAccessCheckerImpl
import org.talkdesk.conversation.auth.impl.data.repository.AuthRepositoryImpl
import org.talkdesk.conversation.auth.impl.data.AuthSessionImpl
import org.talkdesk.conversation.auth.impl.data.local.AuthStorage
import org.talkdesk.conversation.auth.impl.data.ClientAssertionFactory
import org.talkdesk.conversation.auth.impl.data.local.DataStoreAuthStorage
import org.talkdesk.conversation.auth.impl.data.remote.TalkdeskIdApi
import org.talkdesk.conversation.auth.impl.AuthConfig
import org.talkdesk.conversation.auth.impl.domain.repository.AgentAccessChecker
import org.talkdesk.conversation.auth.impl.domain.repository.AuthRepository
import org.talkdesk.conversation.auth.impl.domain.usecase.CancelSignInUseCase
import org.talkdesk.conversation.auth.impl.domain.usecase.CompleteSignInUseCase
import org.talkdesk.conversation.auth.impl.domain.usecase.ForgetAccountUseCase
import org.talkdesk.conversation.auth.impl.domain.usecase.LogoutUseCase
import org.talkdesk.conversation.auth.impl.domain.usecase.RestoreSessionUseCase
import org.talkdesk.conversation.auth.impl.domain.usecase.StartSignInUseCase
import org.talkdesk.conversation.auth.impl.presentation.LoginViewModel
import org.talkdesk.conversation.core.network.ApiBaseUrl
import org.talkdesk.conversation.core.network.AuthorizedHttpClient
import org.talkdesk.conversation.core.network.plugins.withBearerRefresh

/** Qualifier of the [DataStore] the host app must provide for auth storage. */
val AuthDataStore = named("auth.datastore")

/**
 * Also provides the authenticated client ([AuthorizedHttpClient]) and [ApiBaseUrl] for other features.
 * The host app must provide: `HttpClient` (base, unauthenticated), `DataStore<Preferences>`
 * qualified with [AuthDataStore], `OAuthCredentialsProvider` and `Es256Signer`.
 */
val authModule = module {
    single<AuthStorage> { DataStoreAuthStorage(get<DataStore<Preferences>>(AuthDataStore)) }
    single { ClientAssertionFactory(signer = get()) }
    single { TalkdeskIdApi(client = get<HttpClient>(), assertions = get()) }
    single<AuthRepository> {
        AuthRepositoryImpl(api = get(), storage = get(), credentialsProvider = get())
    }

    // Authenticated client: bearer header + refresh-on-expired (single-flight, one retry).
    single<HttpClient>(AuthorizedHttpClient) {
        val repository = get<AuthRepository>()
        get<HttpClient>().withBearerRefresh {
            currentToken = repository::currentAccessToken
            refreshToken = { repository.refreshAccessToken() }
            shouldRefresh = { it.code == AuthConfig.ERROR_TOKEN_EXPIRED }
            onError = { if (it.code == AuthConfig.ERROR_MISSING_SCOPES) repository.revoke() }
        }
    }
    // Gateway of the signed-in account, for features that call the Talkdesk API.
    single<ApiBaseUrl> {
        val storage = get<AuthStorage>()
        ApiBaseUrl {
            val gateway = storage.read().regional?.gateway ?: error("No regional gateway: not signed in")
            "${AuthConfig.PROTOCOL}$gateway"
        }
    }
    single<AgentAccessChecker> { AgentAccessCheckerImpl(get<HttpClient>(AuthorizedHttpClient), get()) }

    factory { RestoreSessionUseCase(get()) }
    factory { StartSignInUseCase(get()) }
    factory { CancelSignInUseCase(get()) }
    factory { CompleteSignInUseCase(get(), get()) }
    factory { LogoutUseCase(get()) }
    factory { ForgetAccountUseCase(get()) }

    single<AuthSession> { AuthSessionImpl(get(), get()) }
    viewModel { LoginViewModel(get(), get(), get(), get(), get(), get()) }
}
