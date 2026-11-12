plugins {
    alias(libs.plugins.conversation.android.application.compose)
}

dependencies {
    implementation(project(":sharedUI"))
    implementation(libs.androidx.activity.compose)
}
