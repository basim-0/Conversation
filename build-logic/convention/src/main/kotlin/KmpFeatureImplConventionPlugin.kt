import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.talkdesk.conversation.buildlogic.libs

/**
 * `conversation.kmp.feature.impl` — `<name>/impl`: shared domain, data and MVI ViewModel
 * of a feature, wired with Koin, plus the feature's Jetpack Compose UI in `androidMain`
 * (SwiftUI lives in `iosApp/`). Depends on its own `:api` and `core:*`, never on another `:impl`.
 */
class KmpFeatureImplConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "conversation.kmp.library")
            apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
            apply(plugin = "org.jetbrains.kotlin.plugin.compose")

            // `:<feature>:impl` always implements its own `:<feature>:api`.
            val ownApi = parent?.findProject("${parent?.path}:api")

            extensions.configure<KotlinMultiplatformExtension> {
                // Compose screens use string/drawable resources from `src/androidMain/res`.
                extensions.getByType<KotlinMultiplatformAndroidLibraryTarget>().androidResources {
                    enable = true
                }

                sourceSets.commonMain.dependencies {
                    ownApi?.let { implementation(it) }
                    implementation(libs.findLibrary("koin-core").get())
                    implementation(libs.findLibrary("koin-core-viewmodel").get())
                    implementation(libs.findLibrary("androidx-lifecycle-viewmodel").get())
                    implementation(libs.findLibrary("kotlinx-serialization-json").get())
                    implementation(libs.findLibrary("kotlinx-datetime").get())
                }
                sourceSets.getByName("androidMain").dependencies {
                    implementation(project(":core:designsystem"))
                    implementation(project(":core:ui"))
                    implementation(project.dependencies.platform(libs.findLibrary("androidx-compose-bom").get()))
                    implementation(libs.findLibrary("androidx-compose-ui").get())
                    implementation(libs.findLibrary("androidx-compose-foundation").get())
                    implementation(libs.findLibrary("androidx-compose-ui-tooling-preview").get())
                    implementation(libs.findLibrary("androidx-lifecycle-compose-runtime").get())
                    implementation(libs.findLibrary("androidx-activity-compose").get())
                    implementation(libs.findLibrary("koin-compose-viewmodel").get())
                }
            }
        }
    }
}
