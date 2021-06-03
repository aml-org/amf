import sbt._

import java.io.{File, FileInputStream}
import java.util.Properties
import scala.collection.JavaConverters._

object Versions {
  lazy val versions: Map[String, String] = {
    val props             = new Properties()
    val sourceModeEnabled = java.lang.Boolean.getBoolean("sbt.sourcemode")
    val versionsFile = if (sourceModeEnabled) {
      Common.workspaceDirectory / "amf" / "amf-apicontract.versions"
    } else {
      file("amf-apicontract.versions")
    }
    props.load(new FileInputStream(versionsFile))
    props.entrySet().asScala.map(e => e.getKey.toString -> e.getValue.toString).toMap
  }
}
