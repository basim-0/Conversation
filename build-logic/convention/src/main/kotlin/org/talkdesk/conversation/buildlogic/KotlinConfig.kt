package org.talkdesk.conversation.buildlogic

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal val AppJvmTarget = JvmTarget.JVM_11

/** Kotlin compiler options shared by every module (Android-only and KMP). */
internal fun Project.configureKotlinCompilerOptions(options: KotlinCommonCompilerOptions) {
    val warningsAsErrors = providers.gradleProperty("warningsAsErrors").map(String::toBoolean).orElse(false)
    options.allWarningsAsErrors.set(warningsAsErrors)
    options.freeCompilerArgs.add("-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi")
}

/** Android-only module (AGP 9 built-in Kotlin). */
internal fun Project.configureKotlinAndroid() {
    extensions.configure<KotlinAndroidProjectExtension> {
        compilerOptions.jvmTarget.set(AppJvmTarget)
        configureKotlinCompilerOptions(compilerOptions)
    }
}

/** KMP module: common compiler options for all targets. */
internal fun Project.configureKotlinMultiplatform() {
    extensions.configure<KotlinMultiplatformExtension> {
        compilerOptions {
            configureKotlinCompilerOptions(this)
        }
    }
}
