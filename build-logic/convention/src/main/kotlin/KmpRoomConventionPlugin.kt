import androidx.room.gradle.RoomExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.talkdesk.conversation.buildlogic.libs

/**
 * `conversation.kmp.room` — adds Room (KMP) with the bundled SQLite driver to a `conversation.kmp.library`
 * module: KSP + Room plugins, runtime dependencies, the compiler on every target, and exported schemas.
 */
class KmpRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "conversation.kmp.library")
            apply(plugin = "com.google.devtools.ksp")
            apply(plugin = "androidx.room")

            extensions.configure<RoomExtension> {
                schemaDirectory("$projectDir/schemas")
            }

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.commonMain.dependencies {
                    api(libs.findLibrary("androidx-room-runtime").get())
                    implementation(libs.findLibrary("androidx-sqlite-bundled").get())
                }
            }

            dependencies {
                val compiler = libs.findLibrary("androidx-room-compiler").get()
                add("kspAndroid", compiler)
                add("kspIosArm64", compiler)
                add("kspIosSimulatorArm64", compiler)
            }
        }
    }
}
