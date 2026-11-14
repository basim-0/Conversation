package org.talkdesk.conversation.calling.impl.telecom

import android.content.Context
import android.net.Uri
import android.telecom.DisconnectCause
import androidx.core.telecom.CallAttributesCompat
import androidx.core.telecom.CallControlScope
import androidx.core.telecom.CallEndpointCompat
import androidx.core.telecom.CallsManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.talkdesk.conversation.calling.impl.domain.model.AudioEndpoint
import org.talkdesk.conversation.calling.impl.domain.model.AudioEndpointType
import org.talkdesk.conversation.calling.impl.domain.model.CallDirection
import org.talkdesk.conversation.calling.impl.domain.model.EndReason
import org.talkdesk.conversation.calling.impl.domain.port.SystemCallGateway
import org.talkdesk.conversation.calling.impl.domain.port.SystemCallListener
import java.util.concurrent.ConcurrentHashMap

/**
 * The system call framework through Jetpack Core-Telecom's [CallsManager]. The app mirrors every call to
 * Telecom so Bluetooth, watches and cars can see and control it, and Telecom owns audio routing.
 *
 * `addCall` runs for the whole life of a call inside its own coroutine. Actions for a call are queued on a
 * channel and run inside that coroutine, because [CallControlScope] can only be used there.
 */
internal class TelecomCallGateway(context: Context) : SystemCallGateway {

    private sealed interface Action {
        data object Answer : Action
        data object SetActive : Action
        data object SetInactive : Action
        data class Endpoint(val endpoint: CallEndpointCompat) : Action
        data class Disconnect(val reason: EndReason) : Action
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val callsManager = CallsManager(context.applicationContext)
    private val actions = ConcurrentHashMap<String, Channel<Action>>()
    private val endpoints = ConcurrentHashMap<String, CallEndpointCompat>()

    @Volatile
    private var listener: SystemCallListener? = null

    @Volatile
    private var registered = false

    // Only touched by the collectors of the one call being set up at a time.
    private var available: List<AudioEndpoint> = emptyList()
    private var current: AudioEndpoint? = null

    override fun setListener(listener: SystemCallListener?) {
        this.listener = listener
    }

    override fun addCall(
        callId: String,
        number: String,
        displayName: String?,
        direction: CallDirection,
        onResult: (error: String?) -> Unit,
    ) {
        val queue = Channel<Action>(Channel.UNLIMITED)
        actions[callId] = queue
        scope.launch {
            var reported = false
            fun report(error: String?) {
                if (!reported) {
                    reported = true
                    onResult(error)
                }
            }
            try {
                registerApp()
                val attributes = CallAttributesCompat(
                    displayName = displayName ?: number,
                    address = Uri.fromParts("tel", number, null),
                    direction = if (direction == CallDirection.Incoming) {
                        CallAttributesCompat.DIRECTION_INCOMING
                    } else {
                        CallAttributesCompat.DIRECTION_OUTGOING
                    },
                    callType = CallAttributesCompat.CALL_TYPE_AUDIO_CALL,
                    callCapabilities = CallAttributesCompat.SUPPORTS_SET_INACTIVE,
                )
                // Each lambda must return within 5 s: they only hand the request to the call manager.
                callsManager.addCall(
                    attributes,
                    onAnswer = { listener?.onAnswerRequested(callId) },
                    onDisconnect = { listener?.onDisconnectRequested(callId) },
                    onSetActive = { listener?.onSetActiveRequested(callId) },
                    onSetInactive = { listener?.onSetInactiveRequested(callId) },
                ) {
                    observe(callId)
                    launch { process(queue, callId) }
                    report(null)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // CallException (e.g. another call is being set up), SecurityException, unsupported device...
                report(e.message ?: e::class.java.simpleName)
            } finally {
                actions.remove(callId)
                queue.close()
                report("The call ended before the system accepted it")
            }
        }
    }

    override fun answer(callId: String) = send(callId, Action.Answer)

    override fun setActive(callId: String) = send(callId, Action.SetActive)

    override fun setInactive(callId: String) = send(callId, Action.SetInactive)

    override fun disconnect(callId: String, reason: EndReason) = send(callId, Action.Disconnect(reason))

    override fun requestEndpoint(endpointId: String) {
        val endpoint = endpoints[endpointId] ?: return
        // Endpoints belong to a call: ask through whichever call is live.
        actions.values.firstOrNull()?.trySend(Action.Endpoint(endpoint))
    }

    private fun send(callId: String, action: Action) {
        actions[callId]?.trySend(action)
    }

    private fun registerApp() {
        if (registered) return
        callsManager.registerAppWithTelecom(CallsManager.CAPABILITY_BASELINE)
        registered = true
    }

    /** Reports endpoint and mute changes of the call until it is over. */
    private fun CallControlScope.observe(callId: String) {
        launch {
            availableEndpoints.collect { list ->
                list.forEach { endpoints[it.identifier.toString()] = it }
                available = list.map { it.toDomain() }
                listener?.onEndpointsChanged(available, current)
            }
        }
        launch {
            currentCallEndpoint.collect { endpoint ->
                endpoints[endpoint.identifier.toString()] = endpoint
                current = endpoint.toDomain()
                listener?.onEndpointsChanged(available, current)
            }
        }
        launch { isMuted.collect { listener?.onMuteChanged(callId, it) } }
    }

    /** Runs the queued actions inside the call's own scope. A disconnect ends the call and its collectors. */
    private suspend fun CallControlScope.process(queue: Channel<Action>, callId: String) {
        val callJob: Job = this.coroutineContext[Job]!!
        for (action in queue) {
            try {
                when (action) {
                    Action.Answer -> answer(CallAttributesCompat.CALL_TYPE_AUDIO_CALL)
                    Action.SetActive -> setActive()
                    Action.SetInactive -> setInactive()
                    is Action.Endpoint -> requestEndpointChange(action.endpoint)
                    is Action.Disconnect -> {
                        disconnect(DisconnectCause(action.reason.toCause()))
                        break
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // The call may already be over on the system side; a disconnect still has to end the scope.
                if (action is Action.Disconnect) break
            }
        }
        // `addCall` returns only when every child coroutine is done.
        callJob.cancelChildren()
    }

    private fun EndReason.toCause(): Int = when (this) {
        EndReason.Local, EndReason.Failed -> DisconnectCause.LOCAL
        EndReason.Remote -> DisconnectCause.REMOTE
        EndReason.Missed -> DisconnectCause.MISSED
        EndReason.Rejected -> DisconnectCause.REJECTED
    }

    private fun CallEndpointCompat.toDomain() = AudioEndpoint(
        id = identifier.toString(),
        type = when (type) {
            CallEndpointCompat.TYPE_EARPIECE -> AudioEndpointType.Earpiece
            CallEndpointCompat.TYPE_SPEAKER -> AudioEndpointType.Speaker
            CallEndpointCompat.TYPE_BLUETOOTH -> AudioEndpointType.Bluetooth
            CallEndpointCompat.TYPE_WIRED_HEADSET -> AudioEndpointType.Wired
            else -> AudioEndpointType.Other
        },
        name = name.toString(),
    )
}
