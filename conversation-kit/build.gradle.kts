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
            export(project(":activity:impl"))
            export(project(":account:api"))
            export(project(":account:impl"))
            export(project(":calling:api"))
            // Swift implements calling's platform ports (Twilio, CallKit, phone numbers) against these types.
            export(project(":calling:impl"))
            export(libs.androidx.lifecycle.viewmodel)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":auth:api"))
            api(project(":auth:impl"))
            api(project(":activity:impl"))
            api(project(":account:api"))
            api(project(":account:impl"))
            api(project(":calling:api"))
            api(project(":calling:impl"))
            api(libs.androidx.lifecycle.viewmodel)
            implementation(project(":core:network"))
            implementation(project(":core:datastore"))
            implementation(project(":core:database"))
            implementation(project(":core:push"))
            implementation(libs.koin.core)
        }
    }
}
