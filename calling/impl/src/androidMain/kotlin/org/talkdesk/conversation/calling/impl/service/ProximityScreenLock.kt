package org.talkdesk.conversation.calling.impl.service

import android.content.Context
import android.os.PowerManager

/** Turns the screen off when the phone is held to the ear, while the earpiece is the audio output. */
internal class ProximityScreenLock(context: Context) {
    private val wakeLock: PowerManager.WakeLock? = context.getSystemService(PowerManager::class.java).let { power ->
        if (power.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK)) {
            power.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "talkdesk:call-proximity")
                .apply { setReferenceCounted(false) }
        } else {
            null
        }
    }

    fun set(enabled: Boolean) {
        val lock = wakeLock ?: return
        if (enabled && !lock.isHeld) lock.acquire()
        if (!enabled && lock.isHeld) lock.release(PowerManager.RELEASE_FLAG_WAIT_FOR_NO_PROXIMITY)
    }
}
