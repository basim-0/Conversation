package org.talkdesk.conversation

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform