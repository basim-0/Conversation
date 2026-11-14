package org.talkdesk.conversation.calling.impl.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.talkdesk.conversation.account.api.AgentRepository
import org.talkdesk.conversation.auth.api.AuthSession
import org.talkdesk.conversation.auth.api.SessionState
import org.talkdesk.conversation.calling.impl.data.remote.CallInfoDto
import org.talkdesk.conversation.calling.impl.data.remote.CallingApi
import org.talkdesk.conversation.calling.impl.domain.model.AudioEndpoint
import org.talkdesk.conversation.calling.impl.domain.model.AudioEndpointType
import org.talkdesk.conversation.calling.impl.domain.model.AudioRoutes
import org.talkdesk.conversation.calling.impl.domain.model.CallDirection
import org.talkdesk.conversation.calling.impl.domain.model.CallQuality
import org.talkdesk.conversation.calling.impl.domain.model.EndReason
import org.talkdesk.conversation.calling.impl.domain.model.VoiceInvite
import org.talkdesk.conversation.calling.impl.domain.port.SystemCallGateway
import org.talkdesk.conversation.calling.impl.domain.port.SystemCallListener
import org.talkdesk.conversation.calling.impl.domain.port.VoiceGateway
import org.talkdesk.conversation.calling.impl.domain.port.VoiceListener
import org.talkdesk.conversation.core.network.utils.dataCatching
import org.talkdesk.conversation.core.network.utils.resultCatching
import kotlin.coroutines.resume
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * The single owner of call state. The voice network (Twilio), the system call framework (Telecom) and
 * REST results are all inputs; none of them owns the state.
 *
 * All changes happen on one serial dispatcher, so callbacks that arrive on any thread are applied in
 * order. Methods that are not `suspend` only schedule work and return at once, which is what the system
 * callbacks require (each must return within 5 seconds).
 */
@OptIn(ExperimentalUuidApi::class)
internal class CallManager(
    private val voice: VoiceGateway,
    private val system: SystemCallGateway,
    private val api: CallingApi,
    private val agents: AgentRepository,
    private val session: AuthSession,
    private val nowMillis: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) : VoiceListener, SystemCallListener {

    private val serial = Dispatchers.Default.limitedParallelism(1)
    private val scope = CoroutineScope(SupervisorJob() + serial)

    private val _calls = MutableStateFlow<List<Call>>(emptyList())

    /** Calls in progress, ringing or dialing. A call leaves the list the moment it ends. */
    val calls: StateFlow<List<Call>> = _calls.asStateFlow()

    private val _audioRoutes = MutableStateFlow(AudioRoutes())
    val audioRoutes: StateFlow<AudioRoutes> = _audioRoutes.asStateFlow()

    private val _events = MutableSharedFlow<CallEvent>(extraBufferCapacity = EVENT_BUFFER)
    val events: SharedFlow<CallEvent> = _events.asSharedFlow()

    val hasActiveCall: StateFlow<Boolean> =
        _calls.map { it.isNotEmpty() }.stateIn(scope, SharingStarted.Eagerly, false)

    // Touched only on [serial].
    private val pollJobs = HashMap<String, Job>()
    private val holdInFlight = HashSet<String>()

    init {
        voice.setListener(this)
        system.setListener(this)
    }

    // region Commands from the UI

    /**
     * Prepares an outgoing call to [number] (E.164): fresh token, agent marked busy, call added to the list.
     * Returns once the call exists; connecting continues in the background and ends the call on failure.
     */
    suspend fun placeCall(number: String): Result<Unit> = resultCatching {
        if (withContext(serial) { _calls.value.isNotEmpty() }) throw CallBusyException()
        val agentId = agents.agentId()
        // Best effort, as the legacy app: the server also routes by this status.
        agents.markBusy(OUTBOUND_BUSY_REASON)
        val token = api.voiceToken()
        val call = Call(id = newId(), direction = CallDirection.Outgoing, status = CallStatus.Dialing, number = number)
        val added = withContext(serial) {
            if (_calls.value.isEmpty()) {
                _calls.value = listOf(call)
                true
            } else {
                false
            }
        }
        if (!added) throw CallBusyException()
        scope.launch { startOutgoing(call, token, agentId) }
    }

    fun accept(callId: String) = post { acceptNow(callId, fromSystem = false) }

    fun reject(callId: String) = post { rejectNow(callId) }

    fun hangUp(callId: String) = post { hangUpNow(callId) }

    fun setMuted(callId: String, muted: Boolean) = post { setMutedNow(callId, muted) }

    fun setHold(callId: String, hold: Boolean) = post { setHoldNow(callId, hold) }

    fun sendDigit(callId: String, digit: Char) = post {
        find(callId)?.voiceHandle?.let { voice.sendDigits(it, digit.toString()) }
    }

    /** Speaker on/off: speaker when it is not the current output, otherwise the best other output. */
    fun toggleSpeaker() {
        val routes = _audioRoutes.value
        val target = if (routes.current?.type == AudioEndpointType.Speaker) {
            routes.available.firstOrNull { it.type == AudioEndpointType.Bluetooth || it.type == AudioEndpointType.Wired }
                ?: routes.available.firstOrNull { it.type == AudioEndpointType.Earpiece }
        } else {
            routes.available.firstOrNull { it.type == AudioEndpointType.Speaker }
        }
        target?.let { system.requestEndpoint(it.id) }
    }

    /** Ends every call (sign-out). */
    suspend fun endAll() = withContext(serial) {
        _calls.value.forEach { call ->
            call.voiceHandle?.let { if (call.isRinging) voice.reject(it) else voice.disconnect(it) }
            finish(call.id, if (call.isRinging) EndReason.Rejected else EndReason.Local)
        }
    }

    // endregion

    // region Voice network events

    override fun onInvite(invite: VoiceInvite) {
        scope.launch { handleInvite(invite) }
    }

    private suspend fun handleInvite(invite: VoiceInvite) {
        // A push can start the process with no screen: give the stored session a moment to be restored.
        val state = withTimeoutOrNull(SESSION_WAIT_MILLIS) { session.state.first { it !is SessionState.Loading } }
        if (state !is SessionState.SignedIn) {
            voice.reject(invite.handle)
            return
        }
        if (findByHandle(invite.handle) != null) return
        val params = invite.parameters
        val isAgentCall = params["from_agent_id"] != null
        val call = Call(
            id = newId(),
            direction = CallDirection.Incoming,
            status = CallStatus.Ringing,
            number = params["customer_phone_number"] ?: invite.from.orEmpty(),
            voiceHandle = invite.handle,
            // Agent-to-agent invites carry the id calls-info knows them by.
            providerCallId = if (isAgentCall) params["call_sid"] ?: invite.callSid else invite.callSid,
        )
        _calls.update { it + call }
        startPolling(call.id)
        // The system mirror is best effort for an incoming call: it can ring through our own UI without it.
        scope.launch { addToSystem(call) }
    }

    override fun onInviteCancelled(handle: String) = post {
        findByHandle(handle)?.takeIf { it.isRinging }?.let { finish(it.id, EndReason.Missed) }
    }

    override fun onRinging(handle: String, callSid: String?) = post {
        val call = findByHandle(handle) ?: return@post
        if (callSid != null && call.providerCallId == null) {
            update(call.id) { it.copy(providerCallId = callSid) }
            startPolling(call.id)
        }
    }

    override fun onConnected(handle: String, callSid: String?) = post {
        val call = findByHandle(handle) ?: return@post
        val connected = update(call.id) {
            it.copy(
                status = CallStatus.Active,
                connectedAtMillis = it.connectedAtMillis ?: nowMillis(),
                providerCallId = it.providerCallId ?: callSid,
            )
        } ?: return@post
        if (call.direction == CallDirection.Outgoing) system.setActive(call.id)
        startPolling(call.id)
        connected.providerCallId?.let { sid -> scope.launch { dataCatching { api.registerClient(sid) } } }
        scope.launch { agents.heartbeat() }
    }

    override fun onConnectFailure(handle: String, error: String?) = post {
        findByHandle(handle)?.let { finish(it.id, EndReason.Failed, error) }
    }

    override fun onReconnecting(handle: String) = post {
        findByHandle(handle)?.takeIf { it.status == CallStatus.Active }
            ?.let { update(it.id) { call -> call.copy(status = CallStatus.Reconnecting) } }
    }

    override fun onReconnected(handle: String) = post {
        findByHandle(handle)?.takeIf { it.status == CallStatus.Reconnecting }
            ?.let { update(it.id) { call -> call.copy(status = CallStatus.Active) } }
    }

    override fun onDisconnected(handle: String, error: String?) = post {
        val call = findByHandle(handle) ?: return@post
        val reason = when {
            call.endRequested -> EndReason.Local
            error != null -> EndReason.Failed
            else -> EndReason.Remote
        }
        finish(call.id, reason, error)
    }

    override fun onQualityChanged(handle: String, quality: CallQuality) = post {
        findByHandle(handle)?.let { update(it.id) { call -> call.copy(quality = quality) } }
    }

    // endregion

    // region System (Telecom) events

    override fun onAnswerRequested(callId: String) = post { acceptNow(callId, fromSystem = true) }

    override fun onDisconnectRequested(callId: String) = post {
        val call = find(callId) ?: return@post
        if (call.isRinging) rejectNow(callId) else hangUpNow(callId)
    }

    override fun onSetActiveRequested(callId: String) = post { setHoldNow(callId, hold = false) }

    override fun onSetInactiveRequested(callId: String) = post { setHoldNow(callId, hold = true) }

    override fun onMuteChanged(callId: String, muted: Boolean) = post {
        if (find(callId)?.isMuted != muted) setMutedNow(callId, muted)
    }

    override fun onEndpointsChanged(available: List<AudioEndpoint>, current: AudioEndpoint?) {
        _audioRoutes.value = AudioRoutes(available, current)
    }

    // endregion

    // region Steps (serial dispatcher only)

    private fun post(block: () -> Unit) {
        scope.launch { block() }
    }

    private suspend fun startOutgoing(call: Call, token: String, agentId: String) {
        val error = addToSystem(call)
        if (error != null) {
            finish(call.id, EndReason.Failed, error)
            return
        }
        // The agent may have cancelled while the system was registering the call.
        if (find(call.id) == null) return
        val handle = try {
            voice.connect(
                accessToken = token,
                parameters = mapOf("to" to call.number, "ToNumber" to call.number, "AgentId" to agentId),
            )
        } catch (e: Exception) {
            finish(call.id, EndReason.Failed, e.message)
            return
        }
        if (update(call.id) { it.copy(voiceHandle = handle) } == null) voice.disconnect(handle)
    }

    private suspend fun addToSystem(call: Call): String? = suspendCancellableCoroutine { continuation ->
        system.addCall(call.id, call.number, call.contactName, call.direction) { error ->
            if (continuation.isActive) continuation.resume(error)
        }
    }

    private fun acceptNow(callId: String, fromSystem: Boolean) {
        val call = find(callId) ?: return
        if (!call.isRinging) return
        val handle = call.voiceHandle ?: return
        // "End and accept": the call in progress is ended for the new one.
        _calls.value.filter { it.id != callId && !it.isRinging }.forEach { other ->
            other.voiceHandle?.let(voice::disconnect)
            finish(other.id, EndReason.Local)
        }
        update(callId) { it.copy(status = CallStatus.Connecting) }
        if (!fromSystem) system.answer(callId)
        voice.accept(handle)
    }

    private fun rejectNow(callId: String) {
        val call = find(callId) ?: return
        call.voiceHandle?.let(voice::reject)
        finish(callId, EndReason.Rejected)
    }

    private fun hangUpNow(callId: String) {
        val call = find(callId) ?: return
        if (call.isRinging) {
            rejectNow(callId)
            return
        }
        val handle = call.voiceHandle
        if (handle == null) {
            // Not connected yet: nothing to tear down on the network.
            finish(callId, EndReason.Local)
            return
        }
        update(callId) { it.copy(endRequested = true) }
        scope.launch { endOnServerThenMedia(call, handle) }
    }

    /** The legacy order: the server ends a connected call, the media leg is the fallback. */
    private suspend fun endOnServerThenMedia(call: Call, handle: String) {
        val providerCallId = call.providerCallId
        val endedOnServer = call.status != CallStatus.Dialing && providerCallId != null &&
            dataCatching { api.disconnect(providerCallId, call.interactionId) }.isSuccess
        if (!endedOnServer) {
            voice.disconnect(handle)
            return
        }
        // The network normally reports the disconnect by itself; make sure the call really ends.
        val ended = withTimeoutOrNull(SERVER_DISCONNECT_GRACE_MILLIS) {
            _calls.first { list -> list.none { it.id == call.id } }
        }
        if (ended == null) voice.disconnect(handle)
    }

    private fun setMutedNow(callId: String, muted: Boolean) {
        val call = update(callId) { it.copy(isMuted = muted) } ?: return
        call.voiceHandle?.let { voice.setMuted(it, muted) }
    }

    private fun setHoldNow(callId: String, hold: Boolean) {
        val call = find(callId) ?: return
        val interactionId = call.interactionId ?: return
        val expected = if (hold) CallStatus.Active else CallStatus.OnHold
        if (call.status != expected || !holdInFlight.add(callId)) return
        scope.launch {
            val result = dataCatching { if (hold) api.hold(interactionId) else api.unhold(interactionId) }
            holdInFlight.remove(callId)
            if (result.isSuccess && find(callId) != null) {
                update(callId) { it.copy(status = if (hold) CallStatus.OnHold else CallStatus.Active) }
                if (hold) system.setInactive(callId) else system.setActive(callId)
            }
        }
    }

    /** Asks `calls-info` for the contact, line and interaction until the call is answered. */
    private fun startPolling(callId: String) {
        val providerCallId = find(callId)?.providerCallId ?: return
        if (pollJobs.containsKey(callId)) return
        pollJobs[callId] = scope.launch {
            repeat(INFO_MAX_ATTEMPTS) {
                val info = dataCatching { api.callInfo(providerCallId) }.getOrNull()
                if (info != null) {
                    if (update(callId) { it.withInfo(info) } == null) return@launch
                    if (info.isAnswered) return@launch
                }
                delay(INFO_DELAY_MILLIS)
            }
        }
    }

    private fun finish(callId: String, reason: EndReason, failure: String? = null) {
        var removed: Call? = null
        _calls.update { list ->
            removed = null
            list.filter { call -> if (call.id == callId) { removed = call; false } else true }
        }
        val call = removed ?: return
        pollJobs.remove(callId)?.cancel()
        holdInFlight.remove(callId)
        system.disconnect(callId, reason)
        _events.tryEmit(CallEvent.Ended(call, reason, failure))
    }

    private fun find(callId: String): Call? = _calls.value.firstOrNull { it.id == callId }

    private fun findByHandle(handle: String): Call? = _calls.value.firstOrNull { it.voiceHandle == handle }

    private fun update(callId: String, change: (Call) -> Call): Call? {
        var result: Call? = null
        _calls.update { list ->
            result = null
            list.map { call -> if (call.id == callId) change(call).also { result = it } else call }
        }
        return result
    }

    private fun newId(): String = Uuid.random().toString()

    // endregion

    private companion object {
        const val OUTBOUND_BUSY_REASON = "Mobile App: user making outbound call"
        const val INFO_MAX_ATTEMPTS = 60
        const val INFO_DELAY_MILLIS = 1_000L
        const val SERVER_DISCONNECT_GRACE_MILLIS = 3_000L
        const val EVENT_BUFFER = 16
        const val SESSION_WAIT_MILLIS = 3_000L
    }
}

private fun Call.withInfo(info: CallInfoDto): Call {
    val contact = info.contact
    val phone = contact?.phones?.firstOrNull { it.number == info.phoneNumber } ?: contact?.phones?.firstOrNull()
    return copy(
        interactionId = info.id ?: interactionId,
        contactName = contact?.name?.takeIf { it.isNotBlank() } ?: info.contactAgentName ?: contactName,
        phoneType = phone?.label?.takeIf { it.isNotBlank() } ?: phoneType,
        lineName = info.ringGroups.firstOrNull() ?: lineName,
        number = if (direction == CallDirection.Incoming) info.phoneNumber?.takeIf { it.isNotBlank() } ?: number else number,
    )
}
