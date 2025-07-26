import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused")
abstract class VlcSetupPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val vlcSetupExtension = project.extensions.create("vlcPluginsLinuxPrepare", VlcSetupExtension::class.java)
        val vlcDownload = project.tasks.register("vlcDownload", VlcDownloadTask::class.java)
        val vlcExtract = project.tasks.register("vlcExtract", VlcExtractTask::class.java) {
            it.dependsOn(vlcDownload)
            it.sourceSnapFile.set(vlcDownload.get().vlcSnapFile)
            it.extractDirectory.set(vlcDownload.get().vlcSnapFile.map { it.resolveSibling("vlc") })
        }
        project.tasks.register("vlcPreparePlugins", VlcPreparePluginsTask::class.java) {
            it.dependsOn(vlcExtract)
            it.sourceDirectory.set(vlcExtract.get().extractDirectory)
            it.targetDirectory.set(vlcSetupExtension.targetCopyPath)
        }
    }
}
