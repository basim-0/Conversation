import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.talkdesk.conversation.buildlogic.libs

/**
 * `conversation.kmp.feature.api` — `<name>/api`: the public contract of a feature
 * (`@Serializable` NavKey routes + the interfaces other features may use). No implementation.
 */
class KmpFeatureApiConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "conversation.kmp.library")
            apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.commonMain.dependencies {
                    api(libs.findLibrary("kotlinx-serialization-core").get())
                }
            }
        }
    }
}
