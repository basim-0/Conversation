package org.talkdesk.conversation.calling.impl.domain.port

import org.talkdesk.conversation.calling.impl.domain.model.AudioEndpoint
import org.talkdesk.conversation.calling.impl.domain.model.CallDirection
import org.talkdesk.conversation.calling.impl.domain.model.EndReason

/**
 * The operating system's call framework: Jetpack Core-Telecom `CallsManager` on Android, CallKit on iOS.
 * The app tells it what happened; it tells the app what the user or the system asked for (a headset
 * button, a watch, a car, another call). Calls are identified by the domain call id.
 *
 * Methods for a call the system does not know are ignored. Callbacks arrive on any thread and the app
 * must return from them quickly.
 */
interface SystemCallGateway {
    fun setListener(listener: SystemCallListener?)

    /** Registers a call with the system. [onResult] gets null once the call is known, or an error message. */
    fun addCall(
        callId: String,
        number: String,
        displayName: String?,
        direction: CallDirection,
        onResult: (error: String?) -> Unit,
    )

    /** An incoming call was answered by the app. */
    fun answer(callId: String)

    fun setActive(callId: String)
    fun setInactive(callId: String)
    fun disconnect(callId: String, reason: EndReason)

    /** Routes the call audio to the endpoint with this [AudioEndpoint.id]. */
    fun requestEndpoint(endpointId: String)
}

interface SystemCallListener {
    /** The system (headset, watch, car) answered the call. */
    fun onAnswerRequested(callId: String)

    /** The system asked to end the call. */
    fun onDisconnectRequested(callId: String)

    /** The system wants the call to continue (e.g. after another call ended). */
    fun onSetActiveRequested(callId: String)

    /** The system wants the call held (e.g. a cellular call was answered). */
    fun onSetInactiveRequested(callId: String)

    /** The device microphone was muted or unmuted from the system. */
    fun onMuteChanged(callId: String, muted: Boolean)

    fun onEndpointsChanged(available: List<AudioEndpoint>, current: AudioEndpoint?)
}
