package org.talkdesk.conversation.auth.impl

/** Talkdesk ID configuration. Single environment (production). */
internal object AuthConfig {
    const val PROTOCOL = "https://"
    const val ENTRYPOINT_HOST = "account.talkdeskid.com"
    const val AUTHORIZE_PATH = "/oauth/authorize"
    const val TOKEN_PATH = "/oauth/token"
    const val END_SESSION_PATH = "/oauth/end_session"
    const val REDIRECT_SCHEME = "com.talkdesk.agent"
    const val REDIRECT_URI = "$REDIRECT_SCHEME://auth"
    const val AGENT_ME_PATH = "/callbar/mobile-app/agents/me"
    const val ASSERTION_LIFETIME_SECONDS = 840L
    const val REFRESH_MAX_ATTEMPTS = 3
    const val BEST_EFFORT_TIMEOUT_MILLIS = 5_000L

    const val ERROR_TOKEN_EXPIRED = "0011011"
    const val ERROR_NOT_WHITELISTED = "0410010"
    const val ERROR_MISSING_SCOPES = "0011016"
}
