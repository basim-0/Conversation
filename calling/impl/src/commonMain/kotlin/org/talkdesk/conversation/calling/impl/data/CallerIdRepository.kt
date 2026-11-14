package org.talkdesk.conversation.calling.impl.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.talkdesk.conversation.calling.impl.data.remote.CallingApi
import org.talkdesk.conversation.core.database.dao.CallerIdDao
import org.talkdesk.conversation.core.database.entity.CallerIdEntity
import org.talkdesk.conversation.core.network.utils.dataCatching

internal data class OutboundCallerId(val number: String, val displayName: String?)

/** The outbound caller ID. Room is the source of truth; the API only writes into it. */
internal class CallerIdRepository(
    private val api: CallingApi,
    private val dao: CallerIdDao,
) {
    fun observe(): Flow<OutboundCallerId?> =
        dao.observe().map { row -> row?.let { OutboundCallerId(it.phoneNumber, it.displayName) } }

    suspend fun refresh(): Result<Unit> = dataCatching {
        val selected = api.selectedCallerId()
        if (selected == null) {
            dao.clear()
        } else {
            dao.upsert(CallerIdEntity(phoneNumber = selected.phoneNumber, displayName = selected.displayName))
        }
    }

    suspend fun clear() = dao.clear()
}
