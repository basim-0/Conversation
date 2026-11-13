import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.talkdesk.conversation.buildlogic.NAMESPACE_ROOT
import org.talkdesk.conversation.buildlogic.configureCommonAndroid
import org.talkdesk.conversation.buildlogic.configureOAuthBuildConfig
import org.talkdesk.conversation.buildlogic.targetSdkVersion

/**
 * `conversation.android.application` — base Android application (AGP 9 built-in Kotlin).
 * Sets namespace/applicationId, version, SDK levels and the release build type; override in the module if needed.
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.android.application")

            extensions.configure<ApplicationExtension> {
                namespace = NAMESPACE_ROOT
                defaultConfig {
                    applicationId = NAMESPACE_ROOT
                    versionCode = 1
                    versionName = "1.0"
                    targetSdk = targetSdkVersion
                }
                configureCommonAndroid(this)
                configureOAuthBuildConfig(this)
                packaging.resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
                buildTypes {
                    release {
                        isMinifyEnabled = false
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro",
                        )
                    }
                }
            }
        }
    }
}
