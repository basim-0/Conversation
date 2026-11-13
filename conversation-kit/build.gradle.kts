import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.conversation.kmp.library)
    alias(libs.plugins.skie)
}

kotlin {
    // The ONE framework iOS consumes. Export only what Swift names; everything else stays `implementation`.
    targets.withType<KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "ConversationKit"
            isStatic = true
            binaryOption("bundleId", "org.talkdesk.conversation.kit")
            export(project(":auth:api"))
            export(project(":auth:impl"))
            export(libs.androidx.lifecycle.viewmodel)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":auth:api"))
            api(project(":auth:impl"))
            api(libs.androidx.lifecycle.viewmodel)
            implementation(project(":core:network"))
            implementation(project(":core:datastore"))
            implementation(libs.koin.core)
        }
    }
}
