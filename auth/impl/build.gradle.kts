plugins {
    alias(libs.plugins.conversation.kmp.feature.impl)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(project(":auth:api"))
    }
}
