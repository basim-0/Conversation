package org.talkdesk.conversation.calling.impl.presentation

import org.talkdesk.conversation.calling.impl.domain.Call

/**
 * Who is on the other end, as the call screens show them. [name] and [initials] are null for an
 * unknown number (the avatar then shows the generic person icon and the number is the title).
 */
data class CallContact(
    val name: String?,
    val initials: String?,
    /** Kind of phone number as the contact defines it, e.g. "Mobile". */
    val phoneType: String?,
    val number: String,
)

internal fun Call.toContact() = CallContact(
    name = contactName,
    initials = contactName?.split(' ')?.filter { it.isNotBlank() }?.take(2)?.joinToString("") { it.first().uppercase() }
        ?.takeIf { it.isNotEmpty() },
    phoneType = phoneType,
    number = number,
)
