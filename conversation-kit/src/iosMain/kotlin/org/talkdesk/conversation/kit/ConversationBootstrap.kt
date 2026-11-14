package org.talkdesk.conversation.kit

import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatform
import org.talkdesk.conversation.account.impl.di.accountIosModule
import org.talkdesk.conversation.account.impl.di.accountModule
import org.talkdesk.conversation.account.impl.presentation.AccountViewModel
import org.talkdesk.conversation.activity.impl.di.activityModule
import org.talkdesk.conversation.activity.impl.presentation.ActivityViewModel
import org.talkdesk.conversation.auth.api.AuthSession
import org.talkdesk.conversation.calling.api.OngoingCallMonitor
import org.talkdesk.conversation.auth.impl.data.Es256Signer
import org.talkdesk.conversation.auth.impl.di.authIosModule
import org.talkdesk.conversation.auth.impl.di.authModule
import org.talkdesk.conversation.auth.impl.domain.OAuthCredentialsProvider
import org.talkdesk.conversation.auth.impl.presentation.LoginViewModel
import org.talkdesk.conversation.calling.impl.di.callingIosModule
import org.talkdesk.conversation.calling.impl.di.callingModule
import org.talkdesk.conversation.calling.impl.domain.port.ClientInfo
import org.talkdesk.conversation.calling.impl.domain.port.PhoneNumberParser
import org.talkdesk.conversation.calling.impl.domain.port.SystemCallGateway
import org.talkdesk.conversation.calling.impl.domain.port.VoiceGateway
import org.talkdesk.conversation.calling.impl.presentation.call.CallViewModel
import org.talkdesk.conversation.calling.impl.presentation.country.CountryPickerViewModel
import org.talkdesk.conversation.calling.impl.presentation.dialer.DialerViewModel
import org.talkdesk.conversation.core.database.databaseIosModule
import org.talkdesk.conversation.core.network.networkIosModule
import org.talkdesk.conversation.core.push.IosPushTokenProvider
import org.talkdesk.conversation.core.push.pushIosModule

/**
 * iOS composition root (counterpart of Android's `ConversationApplication` + `AppModule`).
 * Swift calls [start] once at launch and hands over the pieces only Swift can implement.
 */
/** What only Swift can implement for calling: Twilio, CallKit, phone numbers and the app version. */
class CallingPlatform(
    val voiceGateway: VoiceGateway,
    val systemCallGateway: SystemCallGateway,
    val phoneNumberParser: PhoneNumberParser,
    val clientInfo: ClientInfo,
)

object ConversationBootstrap {
    private var started = false

    fun start(es256Signer: Es256Signer, credentials: OAuthCredentialsProvider, calling: CallingPlatform) {
        if (started) return
        started = true
        startKoin {
            modules(
                networkIosModule,
                databaseIosModule,
                authIosModule,
                authModule,
                activityModule,
                accountModule,
                accountIosModule,
                pushIosModule,
                callingModule,
                callingIosModule,
                module {
                    single<VoiceGateway> { calling.voiceGateway }
                    single<SystemCallGateway> { calling.systemCallGateway }
                    single<PhoneNumberParser> { calling.phoneNumberParser }
                    single<ClientInfo> { calling.clientInfo }
                    single<Es256Signer> { es256Signer }
                    single<OAuthCredentialsProvider> { credentials }
                },
            )
        }
    }

    /** A new login ViewModel. The caller owns it and must call [ScopedViewModel.clear] when done. */
    fun makeLoginViewModel(): ScopedViewModel<LoginViewModel> =
        scopedViewModel { KoinPlatform.getKoin().get<LoginViewModel>() }

    /** A new Activity ViewModel. The caller owns it and must call [ScopedViewModel.clear] when done. */
    fun makeActivityViewModel(): ScopedViewModel<ActivityViewModel> =
        scopedViewModel { KoinPlatform.getKoin().get<ActivityViewModel>() }

    /** A new Account ViewModel. The caller owns it and must call [ScopedViewModel.clear] when done. */
    fun makeAccountViewModel(): ScopedViewModel<AccountViewModel> =
        scopedViewModel { KoinPlatform.getKoin().get<AccountViewModel>() }

    /** A new dialer ViewModel. The caller owns it and must call [ScopedViewModel.clear] when done. */
    fun makeDialerViewModel(): ScopedViewModel<DialerViewModel> =
        scopedViewModel { KoinPlatform.getKoin().get<DialerViewModel>() }

    /** A new call-screen ViewModel. The caller owns it and must call [ScopedViewModel.clear] when done. */
    fun makeCallViewModel(): ScopedViewModel<CallViewModel> =
        scopedViewModel { KoinPlatform.getKoin().get<CallViewModel>() }

    /** A new country-list ViewModel. The caller owns it and must call [ScopedViewModel.clear] when done. */
    fun makeCountryPickerViewModel(): ScopedViewModel<CountryPickerViewModel> =
        scopedViewModel { KoinPlatform.getKoin().get<CountryPickerViewModel>() }

    /** Swift gives every PushKit VoIP token to [IosPushTokenProvider.update]. */
    val pushTokens: IosPushTokenProvider get() = KoinPlatform.getKoin().get()

    /** True from the moment a call rings or dials until it ends. */
    val ongoingCalls: OngoingCallMonitor get() = KoinPlatform.getKoin().get()

    val authSession: AuthSession get() = KoinPlatform.getKoin().get()
}
