rootProject.name = "Conversation"

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        // Talkdesk Osiris SDK (agent status realtime). Needs GITHUB_ACTOR / GITHUB_TOKEN in ~/.gradle/gradle.properties.
        maven {
            name = "TalkdeskOsiris"
            url = uri("https://maven.pkg.github.com/Talkdesk/mobile-osiris-sdk")
            credentials {
                username = providers.gradleProperty("GITHUB_ACTOR").orNull
                password = providers.gradleProperty("GITHUB_TOKEN").orNull
            }
            content { includeGroup("com.talkdesk.mobile") }
        }
    }
}

include(":androidApp")
include(":conversation-kit")
include(":core:designsystem")
include(":core:ui")
include(":core:datastore")
include(":core:network")
include(":core:database")
include(":core:push")
include(":auth:api")
include(":auth:impl")
include(":activity:api")
include(":activity:impl")
include(":home:api")
include(":home:impl")
include(":account:api")
include(":account:impl")
include(":calling:api")
include(":calling:impl")
