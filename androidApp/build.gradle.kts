plugins {
    alias(libs.plugins.conversation.android.application.compose)
}

dependencies {
    implementation(project(":sharedUI"))
    implementation(project(":core:designsystem"))
    implementation(project(":auth:android"))
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
}
