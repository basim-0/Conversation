package org.talkdesk.conversation.auth.api

import kotlinx.coroutines.flow.StateFlow

/** Whether the user is signed in to Talkdesk. */
sealed interface SessionState {
    /** The stored session is still being checked (app start). */
    data object Loading : SessionState

    /**
     * No usable session. [accountName] is the last used account, if any; the login shows Welcome back
     * for it. [sessionExpired] is true when the refresh token expired or the session was revoked
     * (as opposed to an explicit logout).
     */
    data class SignedOut(val accountName: String?, val sessionExpired: Boolean) : SessionState

    data class SignedIn(val accountName: String) : SessionState
}

/** Public entry point of the auth feature for the rest of the app. */
interface AuthSession {
    val state: StateFlow<SessionState>

    /** Ends the session on the server (best effort), clears the stored tokens and signs out. */
    suspend fun logout()
}

/**
 * Work a feature must do while the session is still valid, before the user is signed out
 * (e.g. set the agent offline, unregister from push). Register implementations in Koin as `factory`/`single`
 * bindings of this interface; `logout()` runs all of them in parallel, best effort, with a timeout.
 * Hooks are not run when the session is revoked by the server (the tokens are already dead).
 */
fun interface SignOutHook {
    suspend fun beforeSignOut()
}
