import org.gradle.api.Project
import java.io.File
import javax.inject.Inject

abstract class VlcSetupExtension @Inject constructor(project: Project) {
    val targetCopyPath = project.objects.property(File::class.java)
}
