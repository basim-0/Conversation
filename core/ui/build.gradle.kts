plugins {
    alias(libs.plugins.conversation.android.library.compose)
}

dependencies {
    api(libs.androidx.lifecycle.compose.runtime)
    // Modifier and the shared-transition APIs (foundation brings ui and animation).
    api(libs.androidx.compose.foundation)
}
