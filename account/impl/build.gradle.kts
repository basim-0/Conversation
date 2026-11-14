plugins {
    alias(libs.plugins.conversation.kmp.feature.impl)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":account:api"))
            implementation(project(":auth:api"))
            implementation(project(":calling:api"))
            implementation(project(":core:network"))
            implementation(project(":core:database"))
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.androidx.lifecycle.process)
            implementation(libs.osiris.sdk)
        }
    }
}
