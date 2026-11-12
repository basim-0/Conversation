import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.conversation.kmp.library)
}

// Only the module consumed by Xcode exports a framework; the iOS targets come from the plugin.
kotlin {
    targets.withType<KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "SharedLogic"
            isStatic = true
        }
    }
}
