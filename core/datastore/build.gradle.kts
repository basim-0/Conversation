plugins {
    alias(libs.plugins.conversation.kmp.library)
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(libs.androidx.datastore.preferences.core)
    }
}
