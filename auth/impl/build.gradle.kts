plugins {
    alias(libs.plugins.conversation.kmp.feature.impl)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:network"))
            implementation(project(":core:datastore"))
        }
        androidMain.dependencies {
            implementation(libs.androidx.browser)
            implementation(libs.koin.android)
        }
    }
}
