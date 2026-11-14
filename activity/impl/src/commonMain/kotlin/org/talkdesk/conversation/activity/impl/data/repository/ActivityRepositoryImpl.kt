package org.talkdesk.conversation.activity.impl.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.talkdesk.conversation.activity.impl.data.remote.ActivityApi
import org.talkdesk.conversation.activity.impl.data.remote.ContactDto
import org.talkdesk.conversation.activity.impl.data.remote.InteractionContactDto
import org.talkdesk.conversation.activity.impl.domain.model.Activity
import org.talkdesk.conversation.activity.impl.domain.model.ActivityType
import org.talkdesk.conversation.activity.impl.domain.repository.ActivityRepository
import org.talkdesk.conversation.activity.impl.domain.utils.activityCatching
import org.talkdesk.conversation.core.database.dao.ActivityDao
import org.talkdesk.conversation.core.database.entity.ActivityEntity
import org.talkdesk.conversation.core.network.utils.ApiException
import kotlin.time.Clock
import org.talkdesk.conversation.account.api.AgentRepository
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

internal class ActivityRepositoryImpl(
    private val api: ActivityApi,
    private val dao: ActivityDao,
    private val agents: AgentRepository,
    private val clock: Clock = Clock.System,
) : ActivityRepository {

    // Guards the in-memory paging state below and keeps refresh / load-more from interleaving.
    private val mutex = Mutex()
    private val nextPages = mutableMapOf<Set<ActivityType>, Int?>()

    override fun observe(types: Set<ActivityType>): Flow<List<Activity>> =
        dao.observe(types.map { it.apiName }).map { rows -> rows.mapNotNull { it.toDomain() } }

    override fun observeLastSynced(): Flow<Instant?> =
        dao.observeLastSynced().map { it?.let(Instant::fromEpochMilliseconds) }

    override suspend fun refresh(types: Set<ActivityType>): Result<Boolean> = activityCatching {
        mutex.withLock { fetch(types, page = 1) }
    }

    override suspend fun loadMore(types: Set<ActivityType>): Result<Boolean> = activityCatching {
        mutex.withLock {
            val page = nextPages[types] ?: return@withLock false
            fetch(types, page)
        }
    }

    override suspend fun clear() {
        mutex.withLock {
            nextPages.clear()
            dao.clear()
        }
    }

    /** Fetches [page] of [types] and writes it into the database. Returns whether another page exists. */
    private suspend fun fetch(types: Set<ActivityType>, page: Int): Boolean {
        val now = clock.now()
        val windowStart = now - HISTORY_WINDOW
        val response = api.interactionContacts(
            page = page,
            types = types.map { it.apiName },
            dateFrom = windowStart.toApiTimestamp(),
            dateTo = now.toApiTimestamp(),
        )
        val me = agents.agentId()
        val contacts = contactsOf(response.items)
        val rows = response.items.mapNotNull { it.toEntity(contacts, me, now) }

        if (page == 1) {
            dao.replaceFirstPage(types.map { it.apiName }, rows)
            dao.deleteOlderThan(windowStart.toEpochMilliseconds())
        } else {
            dao.upsert(rows)
        }
        val hasMore = response.page * response.perPage < response.total
        nextPages[types] = if (hasMore) page + 1 else null
        return hasMore
    }

    /** Contacts of a page by id. Best effort: on failure rows keep the number / address as their name. */
    private suspend fun contactsOf(items: List<InteractionContactDto>): Map<String, ContactDto> {
        val ids = items.mapNotNull { it.contactPersonId }.distinct()
        if (ids.isEmpty()) return emptyMap()
        return try {
            api.contacts(ids).embedded?.contacts.orEmpty().associateBy { it.id }
        } catch (_: ApiException) {
            emptyMap()
        }
    }

    private companion object {
        val HISTORY_WINDOW = 90.days
    }
}

private fun InteractionContactDto.toEntity(
    contacts: Map<String, ContactDto>,
    agentId: String,
    syncedAt: Instant,
): ActivityEntity? {
    val activityType = ActivityType.fromApi(type) ?: return null
    val startedAt = (startedAt ?: interactionStartedAt)?.let(::parseApiTimestamp) ?: return null
    val contact = contactPersonId?.let(contacts::get)
    val isEmail = activityType == ActivityType.InboundEmail || activityType == ActivityType.OutboundEmail
    val contactPoint = if (isEmail) contact?.emails?.firstOrNull()?.email else contact?.phones?.firstOrNull()?.number
    val name = contact?.name.takeUnless { it.isNullOrBlank() }
        ?: contactPoint.takeUnless { it.isNullOrBlank() }
        ?: (externalIntegration?.contactPersonChannelId ?: contactNumber).takeUnless { it.isNullOrBlank() }
    return ActivityEntity(
        id = id,
        type = activityType.apiName,
        contactName = name,
        contactNumber = contactNumber ?: contact?.phones?.firstOrNull()?.number,
        agentName = userName,
        isMine = userId != null && userId == agentId,
        startedAtEpochMs = startedAt.toEpochMilliseconds(),
        durationSeconds = duration ?: talkTime ?: 0L,
        syncedAtEpochMs = syncedAt.toEpochMilliseconds(),
    )
}

private fun ActivityEntity.toDomain(): Activity? = Activity(
    id = id,
    type = ActivityType.fromApi(type) ?: return null,
    contactName = contactName,
    contactNumber = contactNumber,
    agentName = agentName,
    isMine = isMine,
    startedAt = Instant.fromEpochMilliseconds(startedAtEpochMs),
    durationSeconds = durationSeconds,
)

/** Timestamps from the API: ISO-8601 in UTC, with or without fraction and offset. */
private fun parseApiTimestamp(text: String): Instant? =
    parseOrNull { Instant.parse(text) } ?: parseOrNull { Instant.parse(text + "Z") }

private inline fun parseOrNull(block: () -> Instant): Instant? =
    try { block() } catch (_: IllegalArgumentException) { null }

/** `yyyy-MM-dd'T'HH:mm:ss.SSS'Z'` in UTC. */
private fun Instant.toApiTimestamp(): String {
    val t = toLocalDateTime(TimeZone.UTC)
    fun Int.pad(length: Int) = toString().padStart(length, '0')
    return "${t.year.pad(4)}-${(t.month.ordinal + 1).pad(2)}-${t.day.pad(2)}" +
        "T${t.hour.pad(2)}:${t.minute.pad(2)}:${t.second.pad(2)}.${(t.nanosecond / 1_000_000).pad(3)}Z"
}
