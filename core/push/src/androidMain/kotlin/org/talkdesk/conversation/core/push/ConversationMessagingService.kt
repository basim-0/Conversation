package org.talkdesk.conversation.core.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.koin.mp.KoinPlatform

/**
 * Entry point of every FCM message. Hands the payload to each registered [PushHandler] and forwards
 * token rotations. Not exported: only Firebase can bind to it.
 */
internal class ConversationMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        val koin = KoinPlatform.getKoinOrNull() ?: return
        val push = PushMessage(message.data)
        koin.getAll<PushHandler>().forEach { handler ->
            // One failing handler must not stop the others.
            runCatching { handler.handle(push) }
        }
    }

    override fun onNewToken(token: String) {
        KoinPlatform.getKoinOrNull()?.get<PushTokenRelay>()?.onNewToken(token)
    }
}
