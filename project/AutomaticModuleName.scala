import sbt.*
import sbt.Keys.*

object AutomaticModuleName {
  def settings(name: String): Seq[Def.Setting[_]] = {
    val pair = ("Automatic-Module-Name" -> name)
    Seq(
      Compile / packageBin / packageOptions += Package.ManifestAttributes(pair)
    )
  }
}
