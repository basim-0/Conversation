package org.talkdesk.conversation.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import org.talkdesk.conversation.core.database.dao.ActivityDao
import org.talkdesk.conversation.core.database.entity.ActivityEntity

/** The app's local database, the single source of truth the UI reads from. */
@Database(entities = [ActivityEntity::class], version = 1, exportSchema = true)
@ConstructedBy(ConversationDatabaseConstructor::class)
abstract class ConversationDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
}

// Room generates the actual implementations.
@Suppress("KotlinNoActualForExpect", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object ConversationDatabaseConstructor : RoomDatabaseConstructor<ConversationDatabase> {
    override fun initialize(): ConversationDatabase
}

internal const val DATABASE_FILE_NAME = "conversation.db"

internal fun RoomDatabase.Builder<ConversationDatabase>.buildDatabase(): ConversationDatabase =
    setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.Default)
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
