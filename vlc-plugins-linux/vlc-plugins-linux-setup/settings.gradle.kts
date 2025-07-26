rootProject.name = "vlc-plugins-linux-setup"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../../gradle/libs.versions.toml"))
        }
    }
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
