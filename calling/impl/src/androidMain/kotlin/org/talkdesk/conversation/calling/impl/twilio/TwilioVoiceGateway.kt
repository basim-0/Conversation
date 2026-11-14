package org.talkdesk.conversation.calling.impl.twilio

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.twilio.voice.AcceptOptions
import com.twilio.voice.Call
import com.twilio.voice.CallException
import com.twilio.voice.CallInvite
import com.twilio.voice.CancelledCallInvite
import com.twilio.voice.ConnectOptions
import com.twilio.voice.MessageListener
import com.twilio.voice.OpusCodec
import com.twilio.voice.PcmuCodec
import com.twilio.voice.RegistrationException
import com.twilio.voice.RegistrationListener
import com.twilio.voice.UnregistrationListener
import com.twilio.voice.Voice
import com.twilio.voice.StatsListener
import com.twilio.voice.StatsReport
import org.talkdesk.conversation.calling.impl.domain.model.CallQuality
import org.talkdesk.conversation.calling.impl.domain.model.VoiceInvite
import org.talkdesk.conversation.calling.impl.domain.port.VoiceGateway
import org.talkdesk.conversation.calling.impl.domain.port.VoiceListener
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * The voice network through the Twilio Voice Android SDK. Media and signalling only: it knows nothing about
 * Telecom or MVI. The SDK is driven from the main thread, so every command is posted there in order.
 */
internal class TwilioVoiceGateway(context: Context) : VoiceGateway {

    private val context = context.applicationContext
    private val main = Handler(Looper.getMainLooper())

    @Volatile
    private var listener: VoiceListener? = null

    // Main thread only.
    private val invites = HashMap<String, CallInvite>()
    private val calls = HashMap<String, Call>()
    private val qualityPolls = ConcurrentHashMap<String, Runnable>()

    override fun setListener(listener: VoiceListener?) {
        this.listener = listener
    }

    /** Hands a Twilio push payload to the SDK, which answers through [messageListener]. */
    fun handlePush(data: Map<String, String>): Boolean = Voice.handleMessage(context, data, messageListener)

    private val messageListener = object : MessageListener {
        override fun onCallInvite(callInvite: CallInvite) {
            main.post { invites[callInvite.callSid] = callInvite }
            listener?.onInvite(
                VoiceInvite(
                    handle = callInvite.callSid,
                    callSid = callInvite.callSid,
                    from = callInvite.from,
                    parameters = callInvite.customParameters,
                ),
            )
        }

        override fun onCancelledCallInvite(cancelledCallInvite: CancelledCallInvite, callException: CallException?) {
            main.post { invites.remove(cancelledCallInvite.callSid) }
            listener?.onInviteCancelled(cancelledCallInvite.callSid)
        }
    }

    override fun register(accessToken: String, pushToken: String, onResult: (error: String?) -> Unit) {
        Voice.register(
            accessToken,
            Voice.RegistrationChannel.FCM,
            pushToken,
            object : RegistrationListener {
                override fun onRegistered(accessToken: String, fcmToken: String) = onResult(null)

                override fun onError(error: RegistrationException, accessToken: String, fcmToken: String) =
                    onResult(error.message ?: "Registration failed")
            },
        )
    }

    override fun unregister(accessToken: String, pushToken: String, onResult: (error: String?) -> Unit) {
        Voice.unregister(
            accessToken,
            Voice.RegistrationChannel.FCM,
            pushToken,
            object : UnregistrationListener {
                override fun onUnregistered(accessToken: String, fcmToken: String) = onResult(null)

                override fun onError(error: RegistrationException, accessToken: String, fcmToken: String) =
                    onResult(error.message ?: "Unregistration failed")
            },
        )
    }

    override fun connect(accessToken: String, parameters: Map<String, String>): String {
        val handle = UUID.randomUUID().toString()
        main.post {
            try {
                val options = ConnectOptions.Builder(accessToken)
                    .params(parameters)
                    .preferAudioCodecs(listOf(OpusCodec(), PcmuCodec()))
                    .build()
                calls[handle] = Voice.connect(context, options, callListener(handle))
            } catch (e: Exception) {
                listener?.onConnectFailure(handle, e.message)
            }
        }
        return handle
    }

    override fun accept(handle: String) {
        main.post {
            val invite = invites.remove(handle)
            if (invite == null) {
                listener?.onConnectFailure(handle, "The call is no longer available")
                return@post
            }
            try {
                calls[handle] = invite.accept(context, AcceptOptions.Builder().build(), callListener(handle))
            } catch (e: Exception) {
                listener?.onConnectFailure(handle, e.message)
            }
        }
    }

    override fun reject(handle: String) {
        main.post { invites.remove(handle)?.reject(context) }
    }

    override fun disconnect(handle: String) {
        main.post { calls[handle]?.disconnect() }
    }

    override fun setMuted(handle: String, muted: Boolean) {
        main.post { calls[handle]?.mute(muted) }
    }

    override fun sendDigits(handle: String, digits: String) {
        main.post { calls[handle]?.sendDigits(digits) }
    }

    private fun callListener(handle: String) = object : Call.Listener {
        override fun onRinging(call: Call) {
            listener?.onRinging(handle, call.sid)
        }

        override fun onConnected(call: Call) {
            startQualityPolling(handle, call)
            listener?.onConnected(handle, call.sid)
        }

        override fun onConnectFailure(call: Call, error: CallException) {
            cleanUp(handle)
            listener?.onConnectFailure(handle, error.message)
        }

        override fun onReconnecting(call: Call, error: CallException) {
            listener?.onReconnecting(handle)
        }

        override fun onReconnected(call: Call) {
            listener?.onReconnected(handle)
        }

        override fun onDisconnected(call: Call, error: CallException?) {
            cleanUp(handle)
            listener?.onDisconnected(handle, error?.message)
        }
    }

    private fun cleanUp(handle: String) {
        qualityPolls.remove(handle)?.let(main::removeCallbacks)
        calls.remove(handle)
    }

    /** Reads the remote audio MOS every [QUALITY_INTERVAL_MILLIS] while connected. */
    private fun startQualityPolling(handle: String, call: Call) {
        val poll = object : Runnable {
            override fun run() {
                if (calls[handle] == null || call.state == Call.State.DISCONNECTED) return
                call.getStats(object : StatsListener {
                    override fun onStats(reports: List<StatsReport>) {
                        val mos = reports.firstOrNull()?.remoteAudioTrackStats?.firstOrNull()?.mos
                        if (mos != null) listener?.onQualityChanged(handle, mos.toDouble().toQuality())
                    }
                })
                main.postDelayed(this, QUALITY_INTERVAL_MILLIS)
            }
        }
        qualityPolls[handle] = poll
        main.post(poll)
    }

    private fun Double.toQuality() = when {
        this > MOS_EXCELLENT -> CallQuality.Excellent
        this >= MOS_GOOD -> CallQuality.Good
        else -> CallQuality.Poor
    }

    private companion object {
        const val QUALITY_INTERVAL_MILLIS = 5_000L
        const val MOS_EXCELLENT = 4.1
        const val MOS_GOOD = 3.7
    }
}
