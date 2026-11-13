package org.talkdesk.conversation.auth.impl.data

import kotlinx.coroutines.flow.StateFlow
import org.talkdesk.conversation.auth.api.AuthSession
import org.talkdesk.conversation.auth.api.SessionState
import org.talkdesk.conversation.auth.impl.domain.repository.AuthRepository
import org.talkdesk.conversation.auth.impl.domain.usecase.LogoutUseCase

internal class AuthSessionImpl(
    repository: AuthRepository,
    private val logoutUseCase: LogoutUseCase,
) : AuthSession {
    override val state: StateFlow<SessionState> = repository.session
    override suspend fun logout() = logoutUseCase()
}
