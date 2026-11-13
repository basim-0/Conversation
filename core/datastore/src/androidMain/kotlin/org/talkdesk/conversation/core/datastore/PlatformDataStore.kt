package org.talkdesk.conversation.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

/** Stores [fileName] (must end with `.preferences_pb`) in the app's private files directory. */
fun createPlatformDataStore(context: Context, fileName: String): DataStore<Preferences> =
    createDataStore { context.applicationContext.filesDir.resolve(fileName).absolutePath }
