package org.talkdesk.conversation.auth.impl.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import org.koin.dsl.module
import org.talkdesk.conversation.core.datastore.createPlatformDataStore

/**
 * iOS binding [authModule] expects from the platform: the auth [DataStore].
 * The host app provides `Es256Signer` (CryptoKit, in Swift) and `OAuthCredentialsProvider`.
 */
val authIosModule = module {
    single<DataStore<Preferences>>(AuthDataStore) {
        createPlatformDataStore(fileName = "auth.preferences_pb")
    }
}
