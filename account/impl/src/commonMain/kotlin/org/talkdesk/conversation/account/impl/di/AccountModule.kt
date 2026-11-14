package org.talkdesk.conversation.account.impl.di

import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import org.talkdesk.conversation.account.api.AgentRepository
import org.talkdesk.conversation.account.impl.data.AccountRepositoryImpl
import org.talkdesk.conversation.account.impl.data.AccountSignOutHook
import org.talkdesk.conversation.account.impl.data.AccountSync
import org.talkdesk.conversation.account.impl.data.remote.AccountApi
import org.talkdesk.conversation.account.impl.domain.AccountRepository
import org.talkdesk.conversation.account.impl.presentation.AccountViewModel
import org.talkdesk.conversation.auth.api.SignOutHook
import org.talkdesk.conversation.calling.api.OngoingCallMonitor
import org.talkdesk.conversation.core.network.AuthorizedHttpClient

/**
 * Bindings of the Account feature, including [AgentRepository] for other features. The host must also list
 * the platform module (`accountAndroidModule` / `accountIosModule`), which provides the status gateway.
 */
val accountModule = module {
    single { AccountApi(client = get<HttpClient>(AuthorizedHttpClient), baseUrl = get()) }
    single<AccountRepository> { AccountRepositoryImpl(api = get(), dao = get()) } bind AgentRepository::class
    single { AccountSignOutHook(repository = get()) } bind SignOutHook::class
    single(createdAtStart = true) { AccountSync(session = get(), repository = get(), gateway = get()) }
    viewModel { AccountViewModel(repository = get(), callMonitor = getOrNull<OngoingCallMonitor>()) }
}
