plugins {
    alias(libs.plugins.conversation.kmp.library)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.koin.core)
            api(project(":core:network"))
        }
        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.messaging)
            implementation(libs.koin.android)
        }
    }
}
