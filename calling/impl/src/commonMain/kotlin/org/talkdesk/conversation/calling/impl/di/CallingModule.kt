package org.talkdesk.conversation.calling.impl.di

import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.talkdesk.conversation.auth.api.SignOutHook
import org.talkdesk.conversation.calling.api.OngoingCallMonitor
import org.talkdesk.conversation.calling.impl.data.CallerIdRepository
import org.talkdesk.conversation.calling.impl.data.CountryRepository
import org.talkdesk.conversation.calling.impl.data.remote.CallingApi
import org.talkdesk.conversation.calling.impl.domain.CallManager
import org.talkdesk.conversation.calling.impl.domain.CallingRegistrar
import org.talkdesk.conversation.calling.impl.domain.CallingSignOutHook
import org.talkdesk.conversation.calling.impl.presentation.call.CallViewModel
import org.talkdesk.conversation.calling.impl.presentation.country.CountryPickerViewModel
import org.talkdesk.conversation.calling.impl.presentation.dialer.DialerViewModel
import org.talkdesk.conversation.calling.impl.presentation.incoming.IncomingCallViewModel
import org.talkdesk.conversation.core.network.AuthorizedHttpClient

/** Qualifier of the [androidx.datastore.core.DataStore] the platform module provides for calling settings. */
val CallingDataStore = named("calling.datastore")

/**
 * Bindings of the Calling feature. The host must also list the platform module (`callingAndroidModule`),
 * the Account module (for `AgentRepository`) and the push module (`pushAndroidModule`). The platform
 * module provides what only the platform can: `VoiceGateway`, `SystemCallGateway`, `PhoneNumberParser`,
 * `ClientInfo` and the settings `DataStore` (qualified with [CallingDataStore]).
 */
val callingModule = module {
    single { CallingApi(client = get<HttpClient>(AuthorizedHttpClient), baseUrl = get(), clientInfo = get()) }
    single { CallerIdRepository(api = get(), dao = get()) }
    single { CountryRepository(parser = get(), store = get(CallingDataStore)) }
    single(createdAtStart = true) {
        CallManager(voice = get(), system = get(), api = get(), agents = get(), session = get())
    }
    single<OngoingCallMonitor> {
        val manager = get<CallManager>()
        object : OngoingCallMonitor {
            override val hasActiveCall = manager.hasActiveCall
        }
    }
    single(createdAtStart = true) {
        CallingRegistrar(
            session = get(),
            agents = get(),
            pushTokens = get(),
            devices = get(),
            api = get(),
            voice = get(),
            callerIds = get(),
        )
    }
    single { CallingSignOutHook(callManager = get(), api = get(), voice = get(), pushTokens = get()) } bind SignOutHook::class
    viewModel { DialerViewModel(calls = get(), callerIds = get(), countries = get(), parser = get()) }
    viewModel { CountryPickerViewModel(countries = get()) }
    viewModel { CallViewModel(calls = get()) }
    viewModel { IncomingCallViewModel(calls = get()) }
}
