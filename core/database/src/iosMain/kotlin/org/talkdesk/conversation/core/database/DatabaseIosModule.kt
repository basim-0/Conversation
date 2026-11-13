package org.talkdesk.conversation.core.database

import androidx.room.Room
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/** iOS binding of [ConversationDatabase] and its DAOs. */
@OptIn(ExperimentalForeignApi::class)
val databaseIosModule = module {
    includes(databaseModule)
    single<ConversationDatabase> {
        val directory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        Room.databaseBuilder<ConversationDatabase>(
            name = requireNotNull(directory).path + "/$DATABASE_FILE_NAME",
        ).buildDatabase()
    }
}
