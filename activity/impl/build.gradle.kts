plugins {
    alias(libs.plugins.conversation.kmp.feature.impl)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":account:api"))
            implementation(project(":auth:api"))
            implementation(project(":core:network"))
            implementation(project(":core:database"))
        }
    }
}
