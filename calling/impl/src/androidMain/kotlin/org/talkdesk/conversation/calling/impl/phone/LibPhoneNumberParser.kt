package org.talkdesk.conversation.calling.impl.phone

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import org.talkdesk.conversation.calling.impl.domain.model.DialCountry
import org.talkdesk.conversation.calling.impl.domain.port.PhoneNumberParser
import java.util.Locale

internal class LibPhoneNumberParser : PhoneNumberParser {
    private val util = PhoneNumberUtil.getInstance()

    override fun toE164(number: String, regionIso: String): String? = try {
        val parsed = util.parse(number, regionIso)
        // Possible is enough: newly allocated ranges are not always "valid" yet.
        if (util.isValidNumber(parsed) || util.isPossibleNumber(parsed)) {
            util.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164)
        } else {
            null
        }
    } catch (_: NumberParseException) {
        null
    }

    /** Every region libphonenumber supports, named in English whatever the device language (as the legacy app). */
    override fun countries(): List<DialCountry> = util.supportedRegions.map { region ->
        DialCountry(
            isoCode = region,
            name = Locale.Builder().setLanguage("en").setRegion(region).build().displayCountry,
            dialCode = "+${util.getCountryCodeForRegion(region)}",
        )
    }

    override fun regionOf(number: String): String? = try {
        util.getRegionCodeForNumber(util.parse(number, ""))
    } catch (_: NumberParseException) {
        null
    }

    override fun formatPartial(number: String, regionIso: String): String {
        val formatter = util.getAsYouTypeFormatter(regionIso)
        var formatted = ""
        number.forEach { formatted = formatter.inputDigit(it) }
        return formatted
    }
}
