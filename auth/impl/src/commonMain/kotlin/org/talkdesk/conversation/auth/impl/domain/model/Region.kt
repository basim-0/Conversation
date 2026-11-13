package org.talkdesk.conversation.auth.impl.domain.model

enum class Region {
    US, CA, EU, AU, UK;

    companion object {
        /** `td-ca` → CA, `td-eu` → EU, `td-ap` → AU, `td-uk` → UK, anything else → US. */
        fun fromTdRegion(tdRegion: String?): Region {
            val value = tdRegion.orEmpty()
            return when {
                value.contains("td-ca") -> CA
                value.contains("td-eu") -> EU
                value.contains("td-ap") -> AU
                value.contains("td-uk") -> UK
                else -> US
            }
        }
    }
}
