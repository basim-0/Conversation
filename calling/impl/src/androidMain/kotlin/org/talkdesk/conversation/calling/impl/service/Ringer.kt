package org.talkdesk.conversation.calling.impl.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Rings and vibrates for an incoming call. Telecom does not ring self-managed calls, so the app does,
 * following the ringer mode like the legacy app: ringtone when normal, vibration only when on vibrate,
 * nothing when silent.
 */
internal class Ringer(private val context: Context) {
    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null
    private var ringing = false

    fun start() {
        if (ringing) return
        ringing = true
        when (context.getSystemService(AudioManager::class.java).ringerMode) {
            AudioManager.RINGER_MODE_NORMAL -> playRingtone()
            AudioManager.RINGER_MODE_VIBRATE -> vibrate()
            else -> Unit
        }
    }

    fun stop() {
        if (!ringing) return
        ringing = false
        ringtone?.stop()
        ringtone = null
        vibrator?.cancel()
        vibrator = null
    }

    private fun playRingtone() {
        ringtone = RingtoneManager.getRingtone(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE))
            ?.apply {
                audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) isLooping = true
                play()
            }
    }

    private fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Vibrator::class.java)
        }
        this.vibrator = vibrator
        vibrator.vibrate(VibrationEffect.createWaveform(VIBRATION_PATTERN, 0))
    }

    private companion object {
        val VIBRATION_PATTERN = longArrayOf(0, 200, 500)
    }
}
