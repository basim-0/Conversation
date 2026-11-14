package org.talkdesk.conversation.calling.impl.service

import android.Manifest
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform
import org.talkdesk.conversation.calling.impl.domain.Call
import org.talkdesk.conversation.calling.impl.domain.CallManager
import org.talkdesk.conversation.calling.impl.domain.CallStatus
import org.talkdesk.conversation.calling.impl.domain.model.AudioEndpointType

/**
 * Foreground service that keeps the process alive during a call and shows its `CallStyle` notification.
 * Type `phoneCall` while ringing or dialing; `microphone` is added once the call is connected and the
 * system allows it. It rings, holds the proximity lock, and stops itself when no call is left.
 */
internal class CallService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var observer: Job? = null
    private lateinit var ringer: Ringer
    private lateinit var proximity: ProximityScreenLock
    private var foregroundType = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        running = true
        ringer = Ringer(this)
        proximity = ProximityScreenLock(this)
        CallNotifications.ensureChannels(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val manager = KoinPlatform.getKoinOrNull()?.get<CallManager>()
        val call = manager?.calls?.value?.let { calls -> calls.firstOrNull { it.isRinging } ?: calls.firstOrNull() }
        // startForeground must follow startForegroundService within seconds, even if the call is already gone.
        if (!promote(call, connected = call != null && !call.isRinging)) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (manager == null || call == null) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (observer == null) observer = scope.launch { observe(manager) }
        return START_NOT_STICKY
    }

    private suspend fun observe(manager: CallManager) {
        combine(manager.calls, manager.audioRoutes) { calls, routes -> calls to routes }.collect { (calls, routes) ->
            val ringing = calls.firstOrNull { it.isRinging }
            val active = calls.firstOrNull { !it.isRinging }
            val primary = ringing ?: active
            if (primary == null) {
                stopSelf()
                return@collect
            }
            if (ringing != null) ringer.start() else ringer.stop()
            proximity.set(
                active != null && ringing == null && active.status != CallStatus.Dialing &&
                    routes.current?.type == AudioEndpointType.Earpiece,
            )
            promote(primary, connected = ringing == null)
        }
    }

    /** Shows the notification and sets the foreground type. Returns false if the system refuses a foreground service. */
    private fun promote(call: Call?, connected: Boolean): Boolean {
        val notification: Notification = when {
            call == null -> CallNotifications.ongoing(this, PLACEHOLDER_CALL)
            call.isRinging -> CallNotifications.incoming(this, call)
            else -> CallNotifications.ongoing(this, call)
        }
        val wantMicrophone = connected && hasMicrophonePermission()
        val type = ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL or
            if (wantMicrophone) ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE else 0
        if (type == foregroundType) {
            runCatching { NotificationManagerCompat.from(this).notify(CallNotifications.NOTIFICATION_ID, notification) }
            return true
        }
        return startForeground(notification, type) ||
            // The microphone type is refused when the app is not visible: phoneCall alone is always allowed.
            (wantMicrophone && startForeground(notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL))
    }

    private fun startForeground(notification: Notification, type: Int): Boolean = try {
        ServiceCompat.startForeground(this, CallNotifications.NOTIFICATION_ID, notification, type)
        foregroundType = type
        true
    } catch (_: SecurityException) {
        false
    } catch (_: IllegalStateException) {
        false
    }

    private fun hasMicrophonePermission() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    override fun onDestroy() {
        ringer.stop()
        proximity.set(false)
        scope.cancel()
        running = false
        super.onDestroy()
    }

    companion object {
        @Volatile
        var running = false
            private set

        /** An empty call used only to build the notification of a service whose call is already gone. */
        private val PLACEHOLDER_CALL = Call(
            id = "",
            direction = org.talkdesk.conversation.calling.impl.domain.model.CallDirection.Outgoing,
            status = CallStatus.Dialing,
            number = "",
        )

        fun start(context: Context) {
            if (running) return
            try {
                ContextCompat.startForegroundService(context, Intent(context, CallService::class.java))
            } catch (_: IllegalStateException) {
                // Background start not allowed: the call UI still works without the service.
            }
        }
    }
}
