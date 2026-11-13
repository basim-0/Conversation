package org.talkdesk.conversation.auth.impl.domain.repository

import kotlinx.coroutines.flow.StateFlow
import org.talkdesk.conversation.auth.api.SessionState
import org.talkdesk.conversation.auth.impl.domain.model.AuthorizationRequest

internal interface AuthRepository {
    val session: StateFlow<SessionState>

    /** Loads the stored session and publishes it on [session]. */
    suspend fun restore()

    /** Validates the account, looks up its region and prepares the browser authorization. */
    suspend fun beginAuthorization(rawAccountName: String): Result<AuthorizationRequest>

    /** Forgets a started authorization (browser cancelled/closed). */
    fun cancelAuthorization()

    /** Exchanges the authorization code in [redirectUri] for tokens and stores them. */
    suspend fun completeAuthorization(redirectUri: String): Result<Unit>

    /** Publishes [SessionState.SignedIn] once the new session has been verified. */
    suspend fun markSignedIn()

    /**
     * Single-flight token refresh. Returns the new access token, or null when it could not be
     * refreshed. Revokes the session when the refresh token is expired or after repeated failures.
     */
    suspend fun refreshAccessToken(): String?

    suspend fun currentAccessToken(): String?

    /** Marks the session as expired and sends the user back to sign in. */
    suspend fun revoke()

    suspend fun logout()

    /** Signs out and forgets the remembered account, so the next sign-in starts from an empty account name. */
    suspend fun forgetAccount()
}


