plugins {
    alias(libs.plugins.conversation.kmp.feature.api)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
        }
    }
}
