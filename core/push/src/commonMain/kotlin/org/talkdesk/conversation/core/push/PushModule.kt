package org.talkdesk.conversation.core.push

import org.koin.core.module.Module
import org.koin.dsl.module
import org.talkdesk.conversation.core.network.AuthorizedHttpClient
import io.ktor.client.HttpClient

/**
 * Shared push bindings: [PushTokenRelay] and [DeviceRegistrationApi]. Included by the platform modules.
 * The platform module must provide [PushPlatform] and [PushTokenProvider].
 */
val pushModule: Module = module {
    single { PushTokenRelay() }
    single { DeviceRegistrationApi(client = get<HttpClient>(AuthorizedHttpClient), baseUrl = get(), platform = get()) }
}
