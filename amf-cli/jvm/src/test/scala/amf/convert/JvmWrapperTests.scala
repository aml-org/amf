package amf.convert
import java.io.File

class JvmWrapperTests extends WrapperTests with NativeOpsFromJvm {
  override def getAbsolutePath(path: String): String = "file://" + new File(path).getAbsolutePath
}
