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
        // JitPack for AuroraOSS gplayapi
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "OmniAPK"
include(":app")
