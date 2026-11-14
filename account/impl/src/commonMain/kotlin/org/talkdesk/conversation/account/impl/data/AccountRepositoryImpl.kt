package org.talkdesk.conversation.account.impl.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.talkdesk.conversation.account.api.Agent
import org.talkdesk.conversation.account.impl.data.remote.AccountApi
import org.talkdesk.conversation.account.impl.data.remote.AgentMeDto
import org.talkdesk.conversation.account.impl.domain.AccountRepository
import org.talkdesk.conversation.account.impl.domain.model.AccountSnapshot
import org.talkdesk.conversation.account.impl.domain.model.StatusInfo
import org.talkdesk.conversation.account.impl.domain.model.StatusType
import org.talkdesk.conversation.core.database.dao.AgentDao
import org.talkdesk.conversation.core.database.entity.AgentEntity
import org.talkdesk.conversation.core.database.entity.AgentStatusEntity
import org.talkdesk.conversation.core.network.utils.dataCatching
import kotlin.time.Clock

/** Room is the source of truth for the profile; the API only writes into it. */
internal class AccountRepositoryImpl(
    private val api: AccountApi,
    private val dao: AgentDao,
    private val nowMillis: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) : AccountRepository {

    private val refreshLock = Mutex()

    override val agent: Flow<Agent?> = dao.observeAgent().map { it?.toAgent() }

    override fun observe(): Flow<AccountSnapshot?> =
        combine(dao.observeAgent(), dao.observeStatuses()) { agent, statuses ->
            agent?.let {
                AccountSnapshot(
                    agent = it.toAgent(),
                    statuses = statuses.map(AgentStatusEntity::toInfo),
                    currentStatusId = it.currentStatusId,
                )
            }
        }

    override suspend fun agentId(): String {
        dao.agent()?.let { return it.id }
        refresh().getOrThrow()
        return checkNotNull(dao.agent()) { "Agent profile is empty after refresh" }.id
    }

    override suspend fun refresh(): Result<Unit> = dataCatching {
        refreshLock.withLock {
            val dto = api.agentMe()
            dao.replaceProfile(dto.toEntity(nowMillis()), dto.statuses.mapIndexed(::toStatusEntity))
        }
    }

    override suspend fun changeStatus(statusId: String): Result<Unit> = dataCatching {
        val agentId = agentId()
        api.changeStatus(agentId, statusId)
        applyRemoteStatus(statusId)
    }

    override suspend fun applyRemoteStatus(statusId: String) {
        val before = dao.agent()?.currentStatusId
        dao.setCurrentStatus(statusId)
        // Like the legacy app: tell the server we are active on the first status and when the agent comes online.
        val statuses = dao.statuses().associateBy { it.id }
        val wasOffline = before == null || StatusType.fromServer(statuses[before]?.type) == StatusType.Offline
        val isOffline = StatusType.fromServer(statuses[statusId]?.type) == StatusType.Offline
        if (wasOffline && !isOffline) heartbeat()
    }

    override suspend fun heartbeat() {
        dataCatching { api.heartbeat() }
    }

    override suspend fun markBusy(reason: String): Result<Unit> = dataCatching { api.setUserStatus("busy", reason) }

    override suspend fun markOffline(reason: String): Result<Unit> = dataCatching { api.setUserStatus("offline", reason) }

    override suspend fun clear() = dao.clear()
}

private fun AgentEntity.toAgent() = Agent(
    id = id,
    accountId = accountId,
    name = name,
    role = role,
    permissions = permissions.split(',').filter { it.isNotBlank() }.toSet(),
)

private fun AgentMeDto.toEntity(syncedAt: Long) = AgentEntity(
    id = id,
    accountId = accountId,
    name = name,
    role = role,
    permissions = permissions.joinToString(","),
    currentStatusId = null,
    syncedAtEpochMs = syncedAt,
)

private fun toStatusEntity(index: Int, dto: org.talkdesk.conversation.account.impl.data.remote.StatusDto) =
    AgentStatusEntity(
        id = dto.id,
        description = dto.description,
        custom = dto.custom,
        type = dto.type.orEmpty(),
        readOnly = dto.readOnly,
        sortOrder = index,
    )

private fun AgentStatusEntity.toInfo() = StatusInfo(
    id = id,
    description = description,
    custom = custom,
    type = StatusType.fromServer(type),
    readOnly = readOnly,
)
