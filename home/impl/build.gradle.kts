plugins {
    alias(libs.plugins.conversation.kmp.feature.impl)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":activity:api"))
        }
        androidMain.dependencies {
            implementation(libs.androidx.navigation3.ui)
        }
    }
}
