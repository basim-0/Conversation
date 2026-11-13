package org.talkdesk.conversation.core.network

import io.ktor.client.HttpClient
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform
import org.koin.dsl.module
import platform.Foundation.NSBundle
import platform.UIKit.UIDevice

/**
 * iOS binding of the base (unauthenticated) [HttpClient].
 * The user agent carries the app version; HTTP logging is on only for debug builds.
 */
@OptIn(ExperimentalNativeApi::class)
val networkIosModule = module {
    single<HttpClient> {
        createHttpClient(userAgent = userAgent(), enableLogging = Platform.isDebugBinary)
    }
}

private fun userAgent(): String {
    val version = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: ""
    return "Conversation/$version iOS/${UIDevice.currentDevice.systemVersion}"
}
