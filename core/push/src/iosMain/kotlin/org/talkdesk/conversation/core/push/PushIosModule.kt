package org.talkdesk.conversation.core.push

import kotlinx.coroutines.flow.Flow
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import platform.Foundation.NSBundle
import kotlin.concurrent.Volatile

/**
 * The push address of this device on iOS. Swift owns PushKit: it hands every VoIP token it receives to
 * [update]. Until the first token arrives [token] is null and the callers retry.
 */
class IosPushTokenProvider(private val relay: PushTokenRelay) : PushTokenProvider {
    @Volatile
    private var latest: String? = null

    override suspend fun token(): String? = latest

    override val tokenChanges: Flow<String> get() = relay.tokenChanges

    /** Called from Swift when PushKit issues or rotates the token (hex string). */
    fun update(token: String) {
        latest = token
        relay.onNewToken(token)
    }
}

/**
 * iOS push bindings (APNs through PushKit). The host only lists this module and gives the PushKit token to
 * [IosPushTokenProvider.update].
 */
val pushIosModule: Module = module {
    includes(pushModule)
    single { PushPlatform(serviceName = "apns", bundle = NSBundle.mainBundle.bundleIdentifier) }
    single { IosPushTokenProvider(relay = get()) } bind PushTokenProvider::class
}
