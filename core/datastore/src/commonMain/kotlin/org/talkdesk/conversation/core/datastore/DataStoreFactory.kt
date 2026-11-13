package org.talkdesk.conversation.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

/**
 * Builds a [DataStore] backed by the file at the path returned by [producePath].
 * Create exactly one instance per file for the lifetime of the process.
 * Platform entry points: `createPlatformDataStore` in `androidMain` / `iosMain`.
 */
fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(produceFile = { producePath().toPath() })
