package org.talkdesk.conversation.account.impl.data.osiris

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.talkdesk.mobile.core.BaseURLProvider
import com.talkdesk.mobile.core.ResultWrapper
import com.talkdesk.mobile.osirissdk.OsirisSDK
import com.talkdesk.mobile.rtmbridgeclient.RTMConnectionCallback
import com.talkdesk.mobile.rtmbridgeclient.RTMProviderConfiguration
import com.talkdesk.mobile.rtmbridgeclient.api.pusher.PusherProviderDefinition
import com.talkdesk.mobile.rtmbridgeclient.pusher.PusherProviderFactoryImpl
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.talkdesk.conversation.account.impl.domain.AgentStatusGateway
import org.talkdesk.conversation.core.network.ApiBaseUrl
import java.io.IOException

/**
 * Live agent status through the Osiris SDK (Pusher). Like the legacy app it is connected only while the app
 * is visible, and drops the connection [BACKGROUND_GRACE_MILLIS] after the app goes to the background.
 */
internal class OsirisAgentStatusGateway(
    client: HttpClient,
    baseUrl: ApiBaseUrl,
) : AgentStatusGateway {

    private val sdk: OsirisSDK by lazy {
        val executor = KtorRequestExecutor(client)
        val baseUrlProvider = object : BaseURLProvider {
            // Osiris appends paths directly, so the base ends with a slash.
            override fun getBaseUrl(): String = runBlocking { baseUrl.get() } + "/"
        }
        OsirisSDK.create(
            requestExecutor = executor,
            baseURLProvider = baseUrlProvider,
            rtmProviderConfiguration = RTMProviderConfiguration(
                "pusher" to (PusherProviderFactoryImpl(executor) to PusherProviderDefinition.serializer()),
            ),
        )
    }

    override fun statusUpdates(agentId: String): Flow<String> =
        appVisible().transformLatest { visible -> if (visible) emitAll(connected(agentId)) }

    /** True while the app is in the foreground; turns false only after the grace period. */
    private fun appVisible(): Flow<Boolean> =
        ProcessLifecycleOwner.get().lifecycle.currentStateFlow
            .map { it.isAtLeast(Lifecycle.State.STARTED) }
            .distinctUntilChanged()
            .transformLatest { visible ->
                if (!visible) delay(BACKGROUND_GRACE_MILLIS)
                emit(visible)
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Main.immediate)

    /** Connects, reads the current status, then follows changes. Disconnects when collection stops. */
    private fun connected(agentId: String): Flow<String> = callbackFlow {
        val rtm = sdk.rtmClient()
        val statusGateway = sdk.agentStatusGateway()
        launch(Dispatchers.IO) {
            try {
                rtm.connect(object : RTMConnectionCallback {
                    override fun connectionSuccessful() {
                        statusGateway.subscribeToAgentStatusChanges(agentId) { result ->
                            when (result) {
                                is ResultWrapper.Success -> trySend(result.value)
                                is ResultWrapper.Failure -> close(result.failure)
                            }
                        }
                        launch(Dispatchers.IO) {
                            try {
                                trySend(statusGateway.getCurrentStatus(agentId))
                            } catch (e: Exception) {
                                close(e)
                            }
                        }
                    }

                    override fun connectionError(message: String) {
                        close(IOException(message))
                    }
                })
            } catch (e: Exception) {
                close(e)
            }
        }
        awaitClose { rtm.disconnect() }
    }

    private companion object {
        const val BACKGROUND_GRACE_MILLIS = 20_000L
    }
}
