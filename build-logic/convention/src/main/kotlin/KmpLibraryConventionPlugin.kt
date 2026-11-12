import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.talkdesk.conversation.buildlogic.AppJvmTarget
import org.talkdesk.conversation.buildlogic.compileSdkVersion
import org.talkdesk.conversation.buildlogic.configureKotlinMultiplatform
import org.talkdesk.conversation.buildlogic.derivedNamespace
import org.talkdesk.conversation.buildlogic.libs
import org.talkdesk.conversation.buildlogic.minSdkVersion

/**
 * `conversation.kmp.library` — shared (below-the-UI) KMP module: android + iOS targets, no UI.
 * Everything lives in `commonMain`; `androidMain`/`iosMain` hold only thin expect/actual glue.
 */
class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "org.jetbrains.kotlin.multiplatform")
            apply(plugin = "com.android.kotlin.multiplatform.library")

            configureKotlinMultiplatform()

            extensions.configure<KotlinMultiplatformExtension> {
                iosArm64()
                iosSimulatorArm64()

                extensions.getByType<KotlinMultiplatformAndroidLibraryTarget>().apply {
                    namespace = derivedNamespace
                    compileSdk = compileSdkVersion
                    minSdk = minSdkVersion
                    compilerOptions.jvmTarget.set(AppJvmTarget)
                    withHostTest { }
                }

                sourceSets.apply {
                    commonMain.dependencies {
                        implementation(libs.findLibrary("kotlinx-coroutines-core").get())
                    }
                    commonTest.dependencies {
                        implementation(libs.findLibrary("kotlin-test").get())
                        implementation(libs.findLibrary("kotlinx-coroutines-test").get())
                    }
                }
            }
        }
    }
}
