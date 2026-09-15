pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "FidelyBar"
include(":app")
include(":core:model")
include(":core:crypto")
include(":core:barcode")
include(":core:data")
include(":core:ui")
include(":core:viewmodel")
include(":feature:home")
include(":feature:editor")
include(":feature:settings")
include(":feature:detail")
include(":feature:onboarding")
 