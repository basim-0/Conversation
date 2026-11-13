package org.talkdesk.conversation.core.database

import org.koin.dsl.module

/** Shared bindings. Included by [databaseAndroidModule] / [databaseIosModule]; hosts list only those. */
internal val databaseModule = module {
    single { get<ConversationDatabase>().activityDao() }
}
