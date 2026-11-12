plugins {
    alias(libs.plugins.conversation.android.application.compose)
}

dependencies {
    implementation(project(":sharedUI"))
    implementation(project(":core:designsystem"))
    implementation(libs.androidx.activity.compose)
}
