package org.talkdesk.conversation.activity.impl.di

import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.talkdesk.conversation.activity.impl.data.ActivitySessionCleaner
import org.talkdesk.conversation.activity.impl.data.remote.ActivityApi
import org.talkdesk.conversation.activity.impl.data.repository.ActivityRepositoryImpl
import org.talkdesk.conversation.activity.impl.domain.repository.ActivityRepository
import org.talkdesk.conversation.activity.impl.presentation.ActivityViewModel
import org.talkdesk.conversation.core.network.AuthorizedHttpClient

/**
 * Bindings of the Activity feature. The host provides nothing itself, but needs these modules started:
 * `authModule` ([AuthorizedHttpClient], `ApiBaseUrl`, `AuthSession`) and the platform database module
 * (`ActivityDao`).
 */
val activityModule = module {
    single { ActivityApi(client = get<HttpClient>(AuthorizedHttpClient), baseUrl = get()) }
    single<ActivityRepository> { ActivityRepositoryImpl(api = get(), dao = get(), agents = get()) }
    single(createdAtStart = true) { ActivitySessionCleaner(session = get(), repository = get()) }
    viewModel { ActivityViewModel(repository = get()) }
}
