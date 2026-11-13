package org.talkdesk.conversation.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/** Stores [fileName] (must end with `.preferences_pb`) in the app's documents directory. */
@OptIn(ExperimentalForeignApi::class)
fun createPlatformDataStore(fileName: String): DataStore<Preferences> = createDataStore {
    val directory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    requireNotNull(directory).path + "/$fileName"
}
