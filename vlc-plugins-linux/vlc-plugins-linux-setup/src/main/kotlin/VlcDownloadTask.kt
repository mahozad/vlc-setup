import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import java.io.File

abstract class VlcDownloadTask : Download() {

    // FIXME: This is used just for the name of the downloaded file;
    //  it does not have any effect on version of VLC downloaded
    //  See the TODO below in the init block
    private val vlcVersion = "3.0.20"

    // Could also have used task-specific `temporaryDir` property (project/build/temp)
    @get:OutputDirectory
    val tempDownloadDirectory: Provider<File> = project.objects.property(File::class.java).value(
        project
            .gradle
            .gradleUserHomeDir
            .resolve("vlc-plugins-linux-setup")
            .also(File::mkdirs)
    )

    @get:OutputFile
    val vlcSnapFile = tempDownloadDirectory.map {
        it.resolve("vlc-$vlcVersion.snap")
    }

    init {
        // Got the download URL from https://search.apps.ubuntu.com/api/v1/package/vlc
        // See the PROJECT.md file in the root project directory for more information
        // TODO: Download the snap version of VLC if/when it is published like for Windows and macOS
        //  See https://code.videolan.org/videolan/vlc/-/issues/29204
        val baseUrl = "https://api.snapcraft.io/api/v1/snaps/download"
        src("$baseUrl/RT9mcUhVsRYrDLG8qnvGiy26NKvv6Qkd_3777.snap")
        dest(vlcSnapFile)
        overwrite(false) // Prevents re-download every time
        readTimeout(60_000) // 1 minute
    }
}
