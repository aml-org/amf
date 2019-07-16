import java.io.FileInputStream
import java.util.Properties

import sbt.Path

import scala.collection.JavaConverters._

object Versions {
  lazy val versions: Map[String, String] = {
    val props = new Properties()
    props.load(new FileInputStream(Path("versions.properties").asFile))
    props.entrySet().asScala.map(e => e.getKey.toString -> e.getValue.toString).toMap
  }
}
