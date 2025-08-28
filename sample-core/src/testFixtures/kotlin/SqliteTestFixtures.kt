import com.alecstrong.sql.psi.sample.core.SampleFileType
import java.net.URI
import java.nio.file.FileSystem
import java.nio.file.FileSystemNotFoundException
import java.nio.file.FileSystems
import java.nio.file.Path
import kotlin.io.path.toPath
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class SqliteTestFixtures : ReadOnlyProperty<Nothing?, Path>, AutoCloseable {
  val jarFile: Path get() = SqliteTestFixtures::class.java.getResource("/SqliteTestFixtures.class")!!.toURI().toJarPath().parent

  override operator fun getValue(thisRef: Nothing?, property: KProperty<*>): Path {
    val uri =
      SqliteTestFixtures::class.java.getResource("/${property.name}.${SampleFileType.defaultExtension}")!!.toURI()
    return uri.toJarPath()
  }

  private val openFileSystems = mutableSetOf<FileSystem>()

  override fun close() {
    for (openFileSystem in openFileSystems) {
      openFileSystem.close()
    }
  }

  private fun URI.toJarPath(): Path {
    val fileSystem = try {
      FileSystems.getFileSystem(this)
    } catch (_: FileSystemNotFoundException) {
      val env = mapOf("create" to "true")
      FileSystems.newFileSystem(this, env)
    }
    openFileSystems.add(fileSystem)
    return toPath()
  }
}
