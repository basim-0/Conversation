package org.talkdesk.conversation.calling.impl.service

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.talkdesk.conversation.calling.impl.domain.CallManager

/** Starts the [CallService] as soon as a call appears. The service stops itself when the calls are gone. */
internal class CallServiceController(context: Context, manager: CallManager) {
    init {
        val appContext = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate).launch {
            manager.calls
                .map { it.isNotEmpty() }
                .distinctUntilChanged()
                .filter { it }
                .collect { CallService.start(appContext) }
        }
    }
}
