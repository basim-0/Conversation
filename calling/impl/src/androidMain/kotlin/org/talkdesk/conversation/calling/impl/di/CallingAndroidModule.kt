package org.talkdesk.conversation.calling.impl.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module
import org.talkdesk.conversation.calling.impl.domain.port.ClientInfo
import org.talkdesk.conversation.calling.impl.domain.port.PhoneNumberParser
import org.talkdesk.conversation.calling.impl.domain.port.SystemCallGateway
import org.talkdesk.conversation.calling.impl.domain.port.VoiceGateway
import org.talkdesk.conversation.calling.impl.phone.LibPhoneNumberParser
import org.talkdesk.conversation.calling.impl.service.CallServiceController
import org.talkdesk.conversation.calling.impl.telecom.TelecomCallGateway
import org.talkdesk.conversation.calling.impl.twilio.TwilioPushHandler
import org.talkdesk.conversation.calling.impl.twilio.TwilioVoiceGateway
import org.talkdesk.conversation.core.datastore.createPlatformDataStore
import org.talkdesk.conversation.core.push.PushHandler

/**
 * Android bindings [callingModule] expects: Core-Telecom, Twilio, libphonenumber and the foreground service.
 * Also registers the push handler for Twilio call invites.
 */
val callingAndroidModule = module {
    single { TelecomCallGateway(androidContext()) } bind SystemCallGateway::class
    single { TwilioVoiceGateway(androidContext()) } bind VoiceGateway::class
    single { TwilioPushHandler(gateway = get()) } bind PushHandler::class
    single<PhoneNumberParser> { LibPhoneNumberParser() }
    single<DataStore<Preferences>>(CallingDataStore) {
        createPlatformDataStore(androidContext(), fileName = "calling.preferences_pb")
    }
    single { ClientInfo(platform = "android", version = androidContext().versionName()) }
    single(createdAtStart = true) { CallServiceController(context = androidContext(), manager = get()) }
}

private fun Context.versionName(): String =
    runCatching { packageManager.getPackageInfo(packageName, 0).versionName }.getOrNull().orEmpty()
