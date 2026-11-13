package org.talkdesk.conversation.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project

private val JavaTarget = JavaVersion.toVersion(AppJvmTarget.target)

/** SDK levels, Java level and Kotlin options shared by every Android application module. */
internal fun Project.configureCommonAndroid(extension: ApplicationExtension) {
    extension.compileOptions {
        sourceCompatibility = JavaTarget
        targetCompatibility = JavaTarget
    }
    configureSdkAndKotlin(extension)
}

/** SDK levels, Java level and Kotlin options shared by every Android-only library module. */
internal fun Project.configureCommonAndroid(extension: LibraryExtension) {
    extension.compileOptions {
        sourceCompatibility = JavaTarget
        targetCompatibility = JavaTarget
    }
    configureSdkAndKotlin(extension)
}

private fun Project.configureSdkAndKotlin(extension: CommonExtension) {
    extension.compileSdk = compileSdkVersion
    extension.defaultConfig.minSdk = minSdkVersion
    configureKotlinAndroid()
}
