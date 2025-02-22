pluginManagement {
    repositories {
        // The order of repositories is important - Android Studio first looks in google(), 
        // then mavenCentral() for dependencies
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}


dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // Same order as above for consistency
        google()
        mavenCentral()
    }

    //versionCatalogs {
        //create("libs") {
            //from(files("gradle/libs.versions.toml"))
        //}
    //}
}

// This tells Gradle what to name your project and which modules to include
rootProject.name = "ForkU"
include(":app")