package org.talkdesk.conversation.kit

import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatform
import org.talkdesk.conversation.auth.api.AuthSession
import org.talkdesk.conversation.auth.impl.data.Es256Signer
import org.talkdesk.conversation.auth.impl.di.authIosModule
import org.talkdesk.conversation.auth.impl.di.authModule
import org.talkdesk.conversation.auth.impl.domain.OAuthCredentialsProvider
import org.talkdesk.conversation.auth.impl.presentation.LoginViewModel
import org.talkdesk.conversation.core.network.networkIosModule

/**
 * iOS composition root (counterpart of Android's `ConversationApplication` + `AppModule`).
 * Swift calls [start] once at launch and hands over the pieces only Swift can implement.
 */
object ConversationBootstrap {
    private var started = false

    fun start(es256Signer: Es256Signer, credentials: OAuthCredentialsProvider) {
        if (started) return
        started = true
        startKoin {
            modules(
                networkIosModule,
                authIosModule,
                authModule,
                module {
                    single<Es256Signer> { es256Signer }
                    single<OAuthCredentialsProvider> { credentials }
                },
            )
        }
    }

    /** A new login ViewModel. The caller owns it and must call [ScopedViewModel.clear] when done. */
    fun makeLoginViewModel(): ScopedViewModel<LoginViewModel> =
        scopedViewModel { KoinPlatform.getKoin().get<LoginViewModel>() }

    val authSession: AuthSession get() = KoinPlatform.getKoin().get()
}
