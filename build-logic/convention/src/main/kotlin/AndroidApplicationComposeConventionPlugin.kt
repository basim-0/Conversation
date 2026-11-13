import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.talkdesk.conversation.buildlogic.libs
import org.talkdesk.conversation.buildlogic.configureAndroidCompose

/**
 * Modules wired into the Android composition root. Add new feature/core modules here,
 * not in `androidApp/build.gradle.kts`.
 */
private val AppModules = listOf(
    ":sharedLogic",
    ":core:designsystem",
    ":core:network",
    ":core:datastore",
    ":auth:api",
    ":auth:impl",
)

/** `conversation.android.application.compose` — Jetpack Compose on top of the application plugin. */
class AndroidApplicationComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "conversation.android.application")
            apply(plugin = "org.jetbrains.kotlin.plugin.compose")

            configureAndroidCompose(extensions.getByType<ApplicationExtension>())

            // Composition root: Activity + Navigation 3 + Koin wiring every app shell needs.
            dependencies {
                AppModules.mapNotNull { rootProject.findProject(it) }
                    .forEach { "implementation"(it) }

                listOf(
                    "androidx-activity-compose",
                    "androidx-navigation3-runtime",
                    "androidx-navigation3-ui",
                    "androidx-lifecycle-compose-runtime",
                    "koin-android",
                    "koin-compose",
                ).forEach { "implementation"(libs.findLibrary(it).get()) }
            }
        }
    }
}
