plugins {
    alias(libs.plugins.conversation.android.feature)
}

dependencies {
    implementation(project(":auth:api"))
    implementation(project(":auth:impl"))
    implementation(project(":core:designsystem"))
}
