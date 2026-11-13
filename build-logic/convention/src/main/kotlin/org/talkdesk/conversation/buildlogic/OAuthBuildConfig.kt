package org.talkdesk.conversation.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Project

/** Region -> Gradle property prefix of the production OAuth client (Agent clients). */
private val OAuthRegionPrefixes = mapOf(
    "US" to "AGENT_PRODUCTION",
    "CA" to "AGENT_CA_PRODUCTION",
    "EU" to "AGENT_EU_PRODUCTION",
    "AU" to "AGENT_AU_PRODUCTION",
    "UK" to "AGENT_UK_PRODUCTION",
)

/** BuildConfig field -> Gradle property suffix. */
private val OAuthFields = mapOf(
    "CLIENT_ID" to "CLIENT_ID",
    "PRIVATE_KEY" to "PRIVATE_KEY",
    "KEY_ID" to "CLIENT_KEY_ID",
)

/**
 * Regional OAuth clients (production) exposed as `BuildConfig.OAUTH_<REGION>_<FIELD>`.
 * Secrets come from `~/.gradle/gradle.properties` and are never committed; a missing property
 * becomes an empty value and sign-in fails for that region.
 */
internal fun Project.configureOAuthBuildConfig(extension: ApplicationExtension) {
    extension.buildFeatures.buildConfig = true
    OAuthRegionPrefixes.forEach { (region, prefix) ->
        OAuthFields.forEach { (field, property) ->
            val value = providers.gradleProperty("${prefix}_$property").getOrElse("")
            extension.defaultConfig.buildConfigField("String", "OAUTH_${region}_$field", "\"$value\"")
        }
    }
}
