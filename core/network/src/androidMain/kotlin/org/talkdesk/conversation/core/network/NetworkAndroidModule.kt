package org.talkdesk.conversation.core.network

import android.content.Context
import android.content.pm.ApplicationInfo
import io.ktor.client.HttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Android binding of the base (unauthenticated) [HttpClient].
 * The user agent carries the app's `versionName`; HTTP logging is on only for debuggable builds.
 */
val networkAndroidModule = module {
    single<HttpClient> {
        val context = androidContext()
        createHttpClient(
            userAgent = context.userAgent(),
            enableLogging = context.isDebuggable(),
        )
    }
}

private fun Context.userAgent(): String {
    val versionName = packageManager.getPackageInfo(packageName, 0).versionName.orEmpty()
    return "Conversation/$versionName ${System.getProperty("http.agent").orEmpty()}".trim()
}

private fun Context.isDebuggable(): Boolean =
    applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
