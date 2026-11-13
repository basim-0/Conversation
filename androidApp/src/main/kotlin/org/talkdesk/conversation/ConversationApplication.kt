package org.talkdesk.conversation

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.talkdesk.conversation.activity.impl.di.activityModule
import org.talkdesk.conversation.auth.impl.di.authAndroidModule
import org.talkdesk.conversation.auth.impl.di.authModule
import org.talkdesk.conversation.core.database.databaseAndroidModule
import org.talkdesk.conversation.core.network.networkAndroidModule
import org.talkdesk.conversation.di.appModule

class ConversationApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ConversationApplication)
            modules(networkAndroidModule, databaseAndroidModule, authAndroidModule, authModule, activityModule, appModule)
        }
    }
}
