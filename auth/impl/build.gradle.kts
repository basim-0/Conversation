plugins {
    alias(libs.plugins.conversation.kmp.feature.impl)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:network"))
            implementation(project(":core:datastore"))
            implementation(libs.koin.core.viewmodel)
        }
        androidMain.dependencies {
            implementation(libs.androidx.browser)
            implementation(libs.koin.android)
        }
    }
}
