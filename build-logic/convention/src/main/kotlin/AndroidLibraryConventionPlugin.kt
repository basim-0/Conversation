import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.talkdesk.conversation.buildlogic.AppJvmTarget
import org.talkdesk.conversation.buildlogic.compileSdkVersion
import org.talkdesk.conversation.buildlogic.configureKotlinAndroid
import org.talkdesk.conversation.buildlogic.derivedNamespace
import org.talkdesk.conversation.buildlogic.libs
import org.talkdesk.conversation.buildlogic.minSdkVersion

/** `conversation.android.library` — Android-only library (designsystem, navigation, feature android UI). */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.android.library")

            extensions.configure<LibraryExtension> {
                namespace = derivedNamespace
                compileSdk = compileSdkVersion
                defaultConfig.minSdk = minSdkVersion
                compileOptions {
                    sourceCompatibility = JavaVersion.toVersion(AppJvmTarget.target)
                    targetCompatibility = JavaVersion.toVersion(AppJvmTarget.target)
                }
            }
            configureKotlinAndroid()

            dependencies {
                "testImplementation"(libs.findLibrary("kotlin-test").get())
                "testImplementation"(libs.findLibrary("kotlinx-coroutines-test").get())
            }
        }
    }
}
