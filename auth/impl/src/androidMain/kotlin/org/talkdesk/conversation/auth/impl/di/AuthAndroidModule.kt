package org.talkdesk.conversation.auth.impl.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.talkdesk.conversation.auth.impl.JavaEs256Signer
import org.talkdesk.conversation.auth.impl.data.Es256Signer
import org.talkdesk.conversation.core.datastore.createPlatformDataStore

/**
 * Android bindings [authModule] expects from the platform: the auth [DataStore] and the ES256 signer.
 * The host app still provides `OAuthCredentialsProvider` (credentials come from its BuildConfig).
 */
val authAndroidModule = module {
    single<DataStore<Preferences>>(AuthDataStore) {
        createPlatformDataStore(androidContext(), fileName = "auth.preferences_pb")
    }
    single<Es256Signer> { JavaEs256Signer() }
}
