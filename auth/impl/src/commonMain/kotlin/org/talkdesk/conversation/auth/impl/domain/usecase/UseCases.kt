package org.talkdesk.conversation.auth.impl.domain.usecase

import org.talkdesk.conversation.auth.impl.domain.repository.AgentAccessChecker
import org.talkdesk.conversation.auth.impl.domain.utils.AuthError
import org.talkdesk.conversation.auth.impl.domain.repository.AuthRepository
import org.talkdesk.conversation.auth.impl.domain.model.AuthorizationRequest

internal class RestoreSessionUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke() = repository.restore()
}

internal class StartSignInUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(accountName: String): Result<AuthorizationRequest> =
        repository.beginAuthorization(accountName)
}

internal class CancelSignInUseCase(private val repository: AuthRepository) {
    operator fun invoke() = repository.cancelAuthorization()
}

/** Finishes the login and verifies the agent is allowed in. A not-whitelisted agent is signed out. */
internal class CompleteSignInUseCase(
    private val repository: AuthRepository,
    private val accessChecker: AgentAccessChecker,
) {
    suspend operator fun invoke(redirectUri: String): Result<Unit> {
        repository.completeAuthorization(redirectUri).onFailure { return Result.failure(it) }
        accessChecker.verify().onFailure {
            if (it is AuthError.NotWhitelisted) repository.logout()
            return Result.failure(it)
        }
        repository.markSignedIn()
        return Result.success(Unit)
    }
}

internal class LogoutUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke() = repository.logout()
}

/** "Use a different account": forgets the remembered account so Welcome back no longer shows. */
internal class ForgetAccountUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke() = repository.forgetAccount()
}
