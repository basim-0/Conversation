import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "org.talkdesk.conversation.buildlogic"

// build-logic itself targets JDK 17; unrelated to the JVM target of the app modules.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.kotlin.composeGradlePlugin)
    compileOnly(libs.kotlin.serializationGradlePlugin)
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = libs.plugins.conversation.android.application.asProvider().get().pluginId
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidApplicationCompose") {
            id = libs.plugins.conversation.android.application.compose.get().pluginId
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }
        register("androidLibrary") {
            id = libs.plugins.conversation.android.library.asProvider().get().pluginId
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = libs.plugins.conversation.android.library.compose.get().pluginId
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
        register("kmpLibrary") {
            id = libs.plugins.conversation.kmp.library.get().pluginId
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("kmpFeatureApi") {
            id = libs.plugins.conversation.kmp.feature.api.get().pluginId
            implementationClass = "KmpFeatureApiConventionPlugin"
        }
        register("kmpFeatureImpl") {
            id = libs.plugins.conversation.kmp.feature.impl.get().pluginId
            implementationClass = "KmpFeatureImplConventionPlugin"
        }
    }
}
