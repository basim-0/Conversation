package org.talkdesk.conversation.calling.impl.domain

import org.talkdesk.conversation.auth.api.SignOutHook
import org.talkdesk.conversation.calling.impl.data.remote.CallingApi
import org.talkdesk.conversation.calling.impl.domain.port.VoiceGateway
import org.talkdesk.conversation.core.push.PushTokenProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** Ends calls and stops the voice network pushing to this device while the session is still valid. */
internal class CallingSignOutHook(
    private val callManager: CallManager,
    private val api: CallingApi,
    private val voice: VoiceGateway,
    private val pushTokens: PushTokenProvider,
) : SignOutHook {
    override suspend fun beforeSignOut() {
        callManager.endAll()
        val pushToken = pushTokens.token() ?: return
        val accessToken = api.voiceToken()
        suspendCancellableCoroutine { continuation ->
            voice.unregister(accessToken, pushToken) { if (continuation.isActive) continuation.resume(Unit) }
        }
    }
}
