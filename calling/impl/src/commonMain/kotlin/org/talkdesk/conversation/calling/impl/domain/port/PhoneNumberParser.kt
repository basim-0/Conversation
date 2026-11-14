package org.talkdesk.conversation.calling.impl.domain.port

import org.talkdesk.conversation.calling.impl.domain.model.DialCountry

/** Phone number rules of the platform (libphonenumber on Android). */
interface PhoneNumberParser {
    /** The number in E.164 (`+15550142`), or null when it is not a valid number in [regionIso]. */
    fun toE164(number: String, regionIso: String): String?

    /** Every country the platform can parse numbers for, with its dial code. Unsorted. */
    fun countries(): List<DialCountry>

    /** The country a number in international form (`+351...`) belongs to, or null while it cannot be told yet. */
    fun regionOf(number: String): String?

    /** [number] formatted for display while it is being typed. A leading `+` and digits are expected. */
    fun formatPartial(number: String, regionIso: String): String
}
