import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

abstract class VlcExtractTask @Inject constructor(
    private val execOperations: ExecOperations
) : DefaultTask() {

    @get:InputFile
    abstract val sourceSnapFile: Property<File>

    @get:OutputDirectory
    abstract val extractDirectory: Property<File>

    /**
     * This tool is needed at least when running on Debian 12.7 because it had not this tool pre-installed.
     * Note that version 4.6.1 did not work on Debian 12.7 because it needed some new version of libraries on system.
     * Acquired the file from https://launchpad.net/ubuntu/+source/squashfs-tools
     * clicking on one of the builds, and downloading the amd64 deb file and then extracting
     * the deb file using dpkg-deb -xv file.deb extraction-dir and grabbed the unsquasfs file.
     * Also, see https://github.com/plougher/squashfs-tools
     * and https://snapshot.debian.org/package/squashfs-tools/1:4.6.1-1/
     */
    private val unsquashfs: File by lazy {
        val destination = temporaryDir.resolve("unsquashfs")
        javaClass
            .getResourceAsStream("/unsquashfs-4.6.1")
            ?.use { input -> destination.outputStream().use(input::copyTo) }
        // See https://stackoverflow.com/a/32331442
        return@lazy destination.apply { setExecutable(true) }
    }

    @TaskAction
    fun execute() {
        execOperations.exec {
            // See https://askubuntu.com/a/1531222
            it.commandLine(
                unsquashfs.absolutePath,
                "-d", extractDirectory.get().absolutePath,
                "-f", // Prevents failure if the -d already exists as Gradle automatically creates it
                sourceSnapFile.get().absolutePath
            )
            // OR
            // it.commandLine(
            //     "file-roller",
            //     "--force",
            //     "--extract-to=${extractDirectory.get().absolutePath}",
            //     snapFile.get().absolutePath
            // )
        }
    }
}
