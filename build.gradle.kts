import org.gradle.api.tasks.wrapper.Wrapper.DistributionType

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.buildConfig)
    alias(libs.plugins.gradlePublish)
}

group = "ir.mahozad"
version = "0.1.0"

dependencies {
    implementation(libs.download.gradlePlugin)
    testImplementation(libs.kotlin.gradlePlugin)
    testImplementation(libs.compose.gradlePlugin)
    testImplementation(libs.junit5)
    testRuntimeOnly(libs.junit5.platformLauncher)
    testImplementation(libs.assertj)
    testImplementation(libs.systemStubs.core)
    testImplementation(libs.systemStubs.jupiter)
}

buildConfig {
    buildConfigField("KOTLIN_VERSION", libs.versions.kotlin.gradlePlugin.get())
    buildConfigField("COMPOSE_VERSION", libs.versions.compose.gradlePlugin.get())
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.wrapper {
    gradleVersion = libs.versions.gradle.get()
    networkTimeout = 60_000 // milliseconds
    distributionType = DistributionType.ALL
    validateDistributionUrl = false
}

gradlePlugin {
    plugins {
        create("vlc-setup") {
            id = "ir.mahozad.vlc-setup"
            implementationClass = "ir.mahozad.vlcsetup.VlcSetupPlugin"
            description = """
                Prepares and builds VLC for Compose Multiplatform desktop applications
                (.dll/.so/.dylib plugin files for Windows, Linux, macOS respectively) 
                to be able to implement a self-contained media player with vlcj library
                without requiring VLC to have been installed on the system. 
            """.trimIndent()
            displayName = "VLC Setup"
            website = "https://github.com/mahozad/vlc-setup"
            vcsUrl = "https://github.com/mahozad/vlc-setup"
            tags = listOf(
                "vlc",
                "vlcj",
                "kotlin-multiplatform",
                "compose-multiplatform",
                "media-player",
                "video-player",
                "audio-player"
            )
        }
    }
}
