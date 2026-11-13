package org.talkdesk.conversation.core.database

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/** Android binding of [ConversationDatabase] and its DAOs. The host only needs `androidContext()`. */
val databaseAndroidModule = module {
    includes(databaseModule)
    single<ConversationDatabase> {
        val context = androidContext()
        Room.databaseBuilder<ConversationDatabase>(
            context = context,
            name = context.getDatabasePath(DATABASE_FILE_NAME).absolutePath,
        ).buildDatabase()
    }
}
