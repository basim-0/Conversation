import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.talkdesk.conversation.buildlogic.libs

/**
 * `conversation.kmp.feature.impl` — `<name>/impl`: shared domain, data and MVI ViewModel
 * of a feature, wired with Koin. Depends on its own `:api` and `core:*`, never on another `:impl`.
 */
class KmpFeatureImplConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "conversation.kmp.library")
            apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.commonMain.dependencies {
                    implementation(libs.findLibrary("koin-core").get())
                    implementation(libs.findLibrary("androidx-lifecycle-viewmodel").get())
                    implementation(libs.findLibrary("kotlinx-serialization-json").get())
                }
                sourceSets.commonTest.dependencies {
                    implementation(libs.findLibrary("koin-test").get())
                }
            }
        }
    }
}
