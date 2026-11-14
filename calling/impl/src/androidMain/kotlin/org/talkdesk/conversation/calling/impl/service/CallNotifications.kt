package org.talkdesk.conversation.calling.impl.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import org.talkdesk.conversation.calling.impl.R
import org.talkdesk.conversation.calling.impl.domain.Call
import org.talkdesk.conversation.calling.impl.ui.CallActivity
import org.talkdesk.conversation.core.designsystem.R as DesignSystemR

/**
 * Notification channels and the `CallStyle` notifications of a call. The channel and notification ids are the
 * legacy app's, so agents keep their per-channel settings after the update.
 */
internal object CallNotifications {
    const val RINGING_CHANNEL_ID = "agent_ringing_channel_id"
    const val ON_CALL_CHANNEL_ID = "agent_on_call_channel_id"
    const val NOTIFICATION_ID = 1001

    fun ensureChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        // Silent on purpose: the app rings and vibrates by itself (Telecom does not ring self-managed calls).
        manager.createNotificationChannel(
            NotificationChannel(
                RINGING_CHANNEL_ID,
                context.getString(R.string.calling_channel_ringing),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = context.getString(R.string.calling_channel_ringing_description)
                setSound(null, null)
                enableVibration(false)
            },
        )
        manager.createNotificationChannel(
            NotificationChannel(
                ON_CALL_CHANNEL_ID,
                context.getString(R.string.calling_channel_on_call),
                NotificationManager.IMPORTANCE_LOW,
            ).apply { description = context.getString(R.string.calling_channel_on_call_description) },
        )
    }

    /** Ringing: full-screen over the lock screen, with Answer and Decline. */
    fun incoming(context: Context, call: Call): Notification {
        val open = activity(context, CallActivity.intent(context), REQUEST_OPEN)
        val answer = activity(context, CallActivity.intent(context, answer = true), REQUEST_ANSWER)
        val decline = broadcast(context, CallActionReceiver.ACTION_DECLINE, REQUEST_DECLINE)
        return NotificationCompat.Builder(context, RINGING_CHANNEL_ID)
            .setSmallIcon(DesignSystemR.drawable.ic_td_phone)
            .setContentTitle(context.title(call))
            .setStyle(NotificationCompat.CallStyle.forIncomingCall(person(context, call), decline, answer))
            .setContentIntent(open)
            .setFullScreenIntent(open, true)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .build()
    }

    /** In progress: a running timer and Hang up. */
    fun ongoing(context: Context, call: Call): Notification {
        val open = activity(context, CallActivity.intent(context), REQUEST_OPEN)
        val hangUp = broadcast(context, CallActionReceiver.ACTION_HANG_UP, REQUEST_HANG_UP)
        val builder = NotificationCompat.Builder(context, ON_CALL_CHANNEL_ID)
            .setSmallIcon(DesignSystemR.drawable.ic_td_phone)
            .setContentTitle(context.title(call))
            .setStyle(NotificationCompat.CallStyle.forOngoingCall(person(context, call), hangUp))
            .setContentIntent(open)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
        call.connectedAtMillis?.let { builder.setWhen(it).setUsesChronometer(true).setShowWhen(true) }
        return builder.build()
    }

    private fun Context.title(call: Call): String =
        call.contactName ?: call.number.ifBlank { getString(R.string.calling_unknown_caller) }

    private fun person(context: Context, call: Call): Person =
        Person.Builder().setName(context.title(call)).setImportant(true).build()

    private fun activity(context: Context, intent: Intent, requestCode: Int): PendingIntent =
        PendingIntent.getActivity(context, requestCode, intent, PENDING_FLAGS)

    /** Explicit and not exported: only this app can trigger the action. */
    private fun broadcast(context: Context, action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, CallActionReceiver::class.java).setAction(action)
        return PendingIntent.getBroadcast(context, requestCode, intent, PENDING_FLAGS)
    }

    private const val PENDING_FLAGS = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    private const val REQUEST_OPEN = 1
    private const val REQUEST_ANSWER = 2
    private const val REQUEST_DECLINE = 3
    private const val REQUEST_HANG_UP = 4
}
