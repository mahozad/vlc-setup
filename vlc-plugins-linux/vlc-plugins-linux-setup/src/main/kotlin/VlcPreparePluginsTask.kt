import org.gradle.api.DefaultTask
import org.gradle.api.file.RelativePath
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import java.nio.file.FileSystems
import javax.inject.Inject
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.copyToRecursively

abstract class VlcPreparePluginsTask @Inject constructor(
    private val execOperations: ExecOperations
) : DefaultTask() {

    /**
     * Do NOT use patchelf v0.18.0 which breaks the so files in Ubuntu 16.04 and 18.04
     * because of https://github.com/NixOS/patchelf/issues/492
     *
     * Acquired the patchelf file from https://github.com/NixOS/patchelf/releases
     * Could instead have used the chrpath tool (can acquire it by `sudo apt install chrpath`
     * and then finding where its binary is with `whereis chrpath` and copying the file) but chrpath
     * does not work if the file does not already have rpath in it (as in libvlccore.so file).
     */
    private val patchelf: File by lazy {
        val destination = temporaryDir.resolve("patchelf")
        javaClass
            .getResourceAsStream("/patchelf-0.17.2" /* Do NOT use 0.18.0; see docs above */)
            ?.use { input -> destination.outputStream().use(input::copyTo) }
        // See https://stackoverflow.com/a/32331442
        return@lazy destination.apply { setExecutable(true) }
    }

    /**
     * List of libraries:
     *   - libidn.so.11   needed in Fedora 41
     *   - libvdpau.so.1  needed in Ubuntu 24.04 and 25.04
     *   - libva.so.2     needed in Ubuntu 18.04
     *   - libva-drm.so.2 needed in Ubuntu 18.04
     *
     * Grab the above .so files from the vlc snap itself
     * (extract the snap file and find the files usually in /usr/lib/x86_64-linux-gnu/)
     * to prevent compatibility errors with other libraries
     * (for example, extracting libidn.so from the rpm downloaded from
     * https://rpmfind.net/linux/rpm2html/search.php?query=libidn.so.11()(64bit)
     * worked on Fedora 41 but did not work on elementary OS 7.1 although the OS worked without this file in the first place)
     */
    @OptIn(ExperimentalPathApi::class)
    private val extraLibraries by lazy {
        // See https://stackoverflow.com/q/11012819
        val libsPath = temporaryDir.resolve("extra-libs").toPath()
        val uri = javaClass
            .classLoader
            .getResource("extra-libs")
            ?.toURI()
            ?: error("Could not get URI/path of extra libraries")
        uri
            // First tries to GET the file system because sometimes it already exists and creating a new one throws exception
            .runCatching { FileSystems.getFileSystem(uri) }
            .recover { FileSystems.newFileSystem(uri, mapOf<String, String>()) }
            .onFailure { println("Could not get or create file system for loading extra libraries: $it") }
            .getOrThrow()
            .getPath("extra-libs")
            .copyToRecursively(target = libsPath, overwrite = true, followLinks = false)
        return@lazy libsPath
    }

    @get:InputDirectory
    abstract val sourceDirectory: Property<File>

    @get:OutputDirectory
    abstract val targetDirectory: Property<File>

    @TaskAction
    fun execute() {
        // TODO: This is so dangerous
        targetDirectory.get().deleteRecursively()
        project.copy { copy ->
            copy.include(
                "usr/lib/libvlc.so",
                "usr/lib/libvlccore.so.9",
                "usr/lib/vlc/plugins/**"
            )
            copy.from(sourceDirectory)
            copy.into(targetDirectory)
            copy.includeEmptyDirs = false
            copy.eachFile {
                // All the below is to strip usr/lib/ from the target copy directory
                // See https://docs.gradle.org/current/userguide/working_with_files.html#sec:unpacking_archives_example
                it.relativePath = RelativePath(true, *it.relativePath.segments.drop(2).toTypedArray())
            }
        }

        project.copy { copy ->
            copy.from(extraLibraries)
            copy.into(targetDirectory)
        }

        /*
        Could also have instead used the below method by installing the chrpath on system
        and loading the script.sh like how the chrpath is loaded above.
        But, installing chrpath using apt requires sudo and it in turn
        requires user password to be entered on the console/terminal.
            project.exec {
                it
                    .commandLine("sh", "$script")
                    .setStandardInput(System.`in`)
                    .workingDir(targetDirectory)
            }
        script.sh content:
            # Good explanation of rpath/runpath and $ORIGIN:
            # https://unix.stackexchange.com/a/22999

            # To install libraries/programs using apt or apt-get, we need to use sudo
            # and, it in turn, needs the user password which is configured to be read
            # from standard input using the -S option and .setStandardInput(System.in)
            # See https://stackoverflow.com/q/21659637
            sudo -S apt install chrpath

            chrpath -r '$ORIGIN' ./libvlc.so

            # Optional step
            # (removing this step does not seem to affect anything but the
            # rpath of the files in plugins/ will be an absolute non-existent path)
            find ./vlc/plugins/ -type f -name "*.so*" | xargs -n1 chrpath -r '$ORIGIN/../../..'
         */
        targetDirectory
            .get()
            .walk()
            .filter { ".so" in it.name }
            .forEach { file ->
                execOperations.exec {
                    it.setIgnoreExitValue(true) // For files that did not contain rpath
                    it.commandLine(
                        patchelf.absolutePath,
                        "--set-rpath",
                        if ("libvlc" in file.name) {
                            "\$ORIGIN" // TODO: Use multi-dollar interpolation in Kotlin 2.2+
                        } else {
                            "\$ORIGIN/../../.." // TODO: Use multi-dollar interpolation in Kotlin 2.2+
                        },
                        file.absolutePath
                    )
                }
            }
    }
}
