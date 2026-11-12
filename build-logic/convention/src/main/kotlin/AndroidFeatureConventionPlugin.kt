import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.talkdesk.conversation.buildlogic.libs

/**
 * `conversation.android.feature` — `<name>/android` modules: the Compose UI of a feature.
 * Renders the shared MVI State and sends Intents; no business logic here.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "conversation.android.library.compose")

            dependencies {
                "implementation"(libs.findLibrary("androidx-compose-ui").get())
                "implementation"(libs.findLibrary("androidx-compose-foundation").get())
                "implementation"(libs.findLibrary("androidx-lifecycle-compose-runtime").get())
                "implementation"(libs.findLibrary("androidx-navigation3-runtime").get())
                "implementation"(libs.findLibrary("koin-compose-viewmodel").get())
            }
        }
    }
}
