package org.talkdesk.conversation.calling.impl.domain.port

/** Identifies this app to Talkdesk call analytics (`"android"` / `"ios"` and the app version). */
class ClientInfo(val platform: String, val version: String)
