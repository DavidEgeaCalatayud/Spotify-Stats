pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SpotifyStats"

include(
    ":app",
    ":core:model",
    ":core:database",
    ":core:designsystem",
    ":domain",
    ":data:history",
)
