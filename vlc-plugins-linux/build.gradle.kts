import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar

plugins {
    id("java-library")
    id("vlc-plugins-linux-setup")
    alias(libs.plugins.mavenPublish)
}

vlcPluginsLinuxPrepare {
    targetCopyPath = projectDir.resolve("src/main/resources/files/")
}

group = "ir.mahozad"
version = "3.0.20-2"

tasks {
    test {
        useJUnitPlatform()
    }
    processResources {
        dependsOn(vlcPreparePlugins)
    }
    wrapper {
        gradleVersion = libs.versions.gradle.get()
        networkTimeout = 60_000 // milliseconds
        distributionType = Wrapper.DistributionType.ALL
        validateDistributionUrl = false
    }
}

publishing {
    repositories {
        maven {
            name = "CustomLocal"
            url = uri("file://${layout.buildDirectory.get()}/local-repository")
        }
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/mahozad/${rootProject.name}")
            credentials(credentialsType = PasswordCredentials::class)
        }
    }
}

mavenPublishing {
    configure(
        platform = JavaLibrary(
            javadocJar = JavadocJar.None(),
            sourcesJar = false,
        )
    )
    publishToMavenCentral(automaticRelease = false)
    signAllPublications()
    coordinates(
        version = project.version.toString(),
        groupId = project.group.toString(),
        artifactId = project.name,
    )

    pom {
        url = "https://central.sonatype.com/artifact/ir.mahozad/vlc-plugins-linux"
        name = project.name
        description = "Publishes self-contained VLC plugins (.so files) to be used as a JVM library for Linux targets."
        inceptionYear = "2024"
        licenses {
            license {
                name = "Apache-2.0 License"
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "mahozad"
                name = "Mahdi Hosseinzadeh"
                url = "https://mahozad.ir/"
                email = ""
                roles = listOf("Lead Developer")
                timezone = "GMT+4:30"
            }
        }
        contributors {
            // contributor {}
        }
        scm {
            tag = "HEAD"
            url = "https://github.com/mahozad/${rootProject.name}"
            connection = "scm:git:github.com/mahozad/${rootProject.name}.git"
            developerConnection = "scm:git:ssh://github.com/mahozad/${rootProject.name}.git"
        }
        issueManagement {
            system = "GitHub"
            url = "https://github.com/mahozad/${rootProject.name}/issues"
        }
        ciManagement {
            system = "GitHub Actions"
            url = "https://github.com/mahozad/${rootProject.name}/actions"
        }
    }
}
