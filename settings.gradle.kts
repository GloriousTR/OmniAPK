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
        // GitLab for AuroraOSS gplayapi
        maven { url = uri("https://gitlab.com/AuroraOSS/repos/-/raw/main/") }
    }
}

rootProject.name = "OmniAPK"
include(":app")
