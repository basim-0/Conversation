package org.talkdesk.conversation.account.impl.di

import org.koin.dsl.module
import org.talkdesk.conversation.account.impl.data.RestAgentStatusGateway
import org.talkdesk.conversation.account.impl.domain.AgentStatusGateway

/** iOS bindings [accountModule] expects. There is no realtime adapter yet: the status is read over REST. */
val accountIosModule = module {
    single<AgentStatusGateway> { RestAgentStatusGateway(api = get()) }
}
