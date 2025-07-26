plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.gradlePublish)
}

dependencies {
    implementation(libs.download.gradlePlugin)
}

kotlin {
    jvmToolchain(libs.versions.java.get().toInt())
}

gradlePlugin {
    plugins {
        create("vlc-plugins-linux-setup") {
            id = "vlc-plugins-linux-setup"
            implementationClass = "VlcSetupPlugin"
        }
    }
}
