package org.talkdesk.conversation.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import org.talkdesk.conversation.core.database.dao.ActivityDao
import org.talkdesk.conversation.core.database.dao.AgentDao
import org.talkdesk.conversation.core.database.dao.CallerIdDao
import org.talkdesk.conversation.core.database.entity.ActivityEntity
import org.talkdesk.conversation.core.database.entity.AgentEntity
import org.talkdesk.conversation.core.database.entity.AgentStatusEntity
import org.talkdesk.conversation.core.database.entity.CallerIdEntity

/** The app's local database, the single source of truth the UI reads from. */
@Database(
    entities = [ActivityEntity::class, AgentEntity::class, AgentStatusEntity::class, CallerIdEntity::class],
    version = 2,
    exportSchema = true,
)
@ConstructedBy(ConversationDatabaseConstructor::class)
abstract class ConversationDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun agentDao(): AgentDao
    abstract fun callerIdDao(): CallerIdDao
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
