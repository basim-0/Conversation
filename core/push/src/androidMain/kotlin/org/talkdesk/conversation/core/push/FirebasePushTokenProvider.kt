package org.talkdesk.conversation.core.push

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.resume

internal class FirebasePushTokenProvider(relay: PushTokenRelay) : PushTokenProvider {
    override val tokenChanges: Flow<String> = relay.tokenChanges

    override suspend fun token(): String? = suspendCancellableCoroutine { continuation ->
        runCatching { FirebaseMessaging.getInstance().token }
            .onFailure { continuation.resume(null) }
            .onSuccess { task ->
                task.addOnCompleteListener { continuation.resume(if (it.isSuccessful) it.result else null) }
            }
    }
}
