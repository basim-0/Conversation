package org.talkdesk.conversation.account.impl.di

import io.ktor.client.HttpClient
import org.koin.dsl.module
import org.talkdesk.conversation.account.impl.data.osiris.OsirisAgentStatusGateway
import org.talkdesk.conversation.account.impl.domain.AgentStatusGateway
import org.talkdesk.conversation.core.network.AuthorizedHttpClient

/** Android bindings [accountModule] expects: the live status source (Osiris realtime channel). */
val accountAndroidModule = module {
    single<AgentStatusGateway> { OsirisAgentStatusGateway(client = get<HttpClient>(AuthorizedHttpClient), baseUrl = get()) }
}
