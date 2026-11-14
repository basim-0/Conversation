package org.talkdesk.conversation.core.push

import org.koin.core.module.Module
import org.koin.dsl.module

/** Android push bindings (FCM). The host only lists this module. */
val pushAndroidModule: Module = module {
    includes(pushModule)
    single { PushPlatform(serviceName = "firebase") }
    single<PushTokenProvider> { FirebasePushTokenProvider(relay = get()) }
}
