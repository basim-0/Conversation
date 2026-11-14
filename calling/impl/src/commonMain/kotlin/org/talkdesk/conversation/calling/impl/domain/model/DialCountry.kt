package org.talkdesk.conversation.calling.impl.domain.model

/** A country the dialer can dial from: ISO 3166 alpha-2 code, English name and international dial code ("+351"). */
data class DialCountry(val isoCode: String, val name: String, val dialCode: String)
