import java.io.FileInputStream
import java.util.Properties

import sbt.Path

import scala.collection.JavaConverters._

object Versions {
  lazy val versions: Map[String, String] = {
    val props        = new Properties()
    val absolutePath = Path("").asFile.getAbsolutePath
    val versionsFile =
      if (absolutePath.endsWith("als") || absolutePath.endsWith("amf-runner") || absolutePath.endsWith("amf-TCKutor"))
        Path("../amf/versions.properties").asFile
      else Path("versions.properties").asFile
    props.load(new FileInputStream(versionsFile))
    props.entrySet().asScala.map(e => e.getKey.toString -> e.getValue.toString).toMap
  }
}
