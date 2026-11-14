plugins {
    alias(libs.plugins.conversation.kmp.feature.impl)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":calling:api"))
            implementation(project(":account:api"))
            implementation(project(":auth:api"))
            implementation(project(":core:network"))
            implementation(project(":core:database"))
            implementation(project(":core:datastore"))
            implementation(project(":core:push"))
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.androidx.core.telecom)
            implementation(libs.twilio.voice.android)
            implementation(libs.libphonenumber)
            implementation(libs.androidx.lifecycle.process)
        }
    }
}
