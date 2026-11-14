package org.talkdesk.conversation.calling.impl.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import org.koin.dsl.module
import org.talkdesk.conversation.core.datastore.createPlatformDataStore

/**
 * iOS bindings [callingModule] expects that Kotlin can provide: the settings `DataStore`. The host must
 * still provide what only Swift can implement: `VoiceGateway` (Twilio), `SystemCallGateway` (CallKit),
 * `PhoneNumberParser` and `ClientInfo`, plus the push module ([org.talkdesk.conversation.core.push.pushIosModule]).
 */
val callingIosModule = module {
    single<DataStore<Preferences>>(CallingDataStore) { createPlatformDataStore(fileName = "calling.preferences_pb") }
}
