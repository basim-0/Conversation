package org.talkdesk.conversation.auth.impl.domain

import org.talkdesk.conversation.auth.impl.AuthConfig
import org.talkdesk.conversation.auth.impl.domain.model.OAuthCredentials
import org.talkdesk.conversation.auth.impl.domain.model.Region

interface OAuthCredentialsProvider {
    fun forRegion(region: Region): OAuthCredentials
}
