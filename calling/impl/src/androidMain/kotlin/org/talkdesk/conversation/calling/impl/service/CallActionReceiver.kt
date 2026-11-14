package org.talkdesk.conversation.calling.impl.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.koin.mp.KoinPlatform
import org.talkdesk.conversation.calling.impl.domain.CallManager

/**
 * Receives the Decline and Hang up buttons of the call notifications. Not exported, and only reached
 * through explicit [android.app.PendingIntent]s created by this app.
 */
internal class CallActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val manager = KoinPlatform.getKoinOrNull()?.get<CallManager>() ?: return
        val calls = manager.calls.value
        when (intent.action) {
            ACTION_DECLINE -> calls.firstOrNull { it.isRinging }?.let { manager.reject(it.id) }
            ACTION_HANG_UP -> calls.firstOrNull { !it.isRinging }?.let { manager.hangUp(it.id) }
        }
    }

    companion object {
        const val ACTION_DECLINE = "org.talkdesk.conversation.calling.action.DECLINE"
        const val ACTION_HANG_UP = "org.talkdesk.conversation.calling.action.HANG_UP"
    }
}
