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
    }
}

include(":androidApp")
include(":conversation-kit")
include(":core:designsystem")
include(":core:ui")
include(":core:datastore")
include(":core:network")
include(":core:database")
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
