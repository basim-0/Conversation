package org.talkdesk.conversation.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.version(alias: String): String = findVersion(alias).get().requiredVersion

internal val Project.compileSdkVersion: Int get() = libs.version("android-compileSdk").toInt()
internal val Project.minSdkVersion: Int get() = libs.version("android-minSdk").toInt()
internal val Project.targetSdkVersion: Int get() = libs.version("android-targetSdk").toInt()

internal const val NAMESPACE_ROOT = "org.talkdesk.conversation"

/** Same id as the legacy production app: keeps the Firebase project, FCM tokens and Twilio push credential. */
internal const val APPLICATION_ID = "com.talkdesk.agent"

/** `:auth:impl` -> `org.talkdesk.conversation.auth.impl` */
internal val Project.derivedNamespace: String
    get() = path.split(':')
        .filter { it.isNotBlank() }
        .joinToString(separator = ".", prefix = "$NAMESPACE_ROOT.") { segment ->
            segment.lowercase().replace("-", "").replace("_", "")
        }
