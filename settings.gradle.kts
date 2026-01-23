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
        // GitLab Maven for AuroraOSS gplayapi
        maven { 
            url = uri("https://gitlab.com/AuroraOSS/gplayapi/-/raw/master/mvn-repo")
            content {
                includeGroup("com.aurora.gplayapi")
            }
        }
        // JitPack for additional dependencies
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "OmniAPK"
include(":app")
