package org.talkdesk.conversation.calling.impl.domain

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.talkdesk.conversation.account.api.AgentRepository
import org.talkdesk.conversation.auth.api.AuthSession
import org.talkdesk.conversation.auth.api.SessionState
import org.talkdesk.conversation.calling.impl.data.CallerIdRepository
import org.talkdesk.conversation.calling.impl.data.remote.CallingApi
import org.talkdesk.conversation.calling.impl.domain.port.VoiceGateway
import org.talkdesk.conversation.core.push.DeviceRegistrationApi
import org.talkdesk.conversation.core.push.PushTokenProvider
import kotlin.coroutines.resume

/**
 * Makes inbound calls possible while the agent is signed in: registers the device with Talkdesk and the
 * push token with the voice network, again whenever the push token changes. Every step retries until it
 * works. Forgets the cached caller ID after sign-out.
 */
internal class CallingRegistrar(
    private val session: AuthSession,
    private val agents: AgentRepository,
    private val pushTokens: PushTokenProvider,
    private val devices: DeviceRegistrationApi,
    private val api: CallingApi,
    private val voice: VoiceGateway,
    private val callerIds: CallerIdRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        scope.launch {
            session.state
                .map { it is SessionState.SignedIn }
                .distinctUntilChanged()
                .collectLatest { signedIn -> if (signedIn) registerWhileSignedIn() }
        }
        scope.launch {
            session.state.filterIsInstance<SessionState.SignedOut>().collect { callerIds.clear() }
        }
    }

    private suspend fun registerWhileSignedIn() = coroutineScope {
        val agentId = retrying { agents.agentId() }
        launch { callerIds.refresh() }
        merge(
            flow { emit(retrying { pushTokens.token() ?: error("No push token yet") }) },
            pushTokens.tokenChanges,
        )
            .distinctUntilChanged()
            .collectLatest { pushToken ->
                coroutineScope {
                    launch { retrying { devices.register(agentId, pushToken) } }
                    launch { retrying { registerVoice(pushToken) } }
                }
            }
    }

    private suspend fun registerVoice(pushToken: String) {
        val accessToken = api.voiceToken()
        val error = suspendCancellableCoroutine { continuation ->
            voice.register(accessToken, pushToken) { if (continuation.isActive) continuation.resume(it) }
        }
        if (error != null) throw IllegalStateException("Voice registration failed: $error")
    }

    private suspend fun <T> retrying(block: suspend () -> T): T {
        while (true) {
            try {
                return block()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                delay(RETRY_DELAY_MILLIS)
            }
        }
    }

    private companion object {
        const val RETRY_DELAY_MILLIS = 5_000L
    }
}
