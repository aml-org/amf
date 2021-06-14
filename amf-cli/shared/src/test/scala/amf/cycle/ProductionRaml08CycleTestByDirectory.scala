package amf.cycle

import amf.core.internal.remote.Syntax.Yaml
import amf.core.internal.remote.{Hint, Raml08}

/**
  * Cycle by directory test for dir: [[amf-cli/shared/src/test/resources/production/raml08/]]
  *   origin: Raml08
  *   target: Raml08
  */
class ProductionRaml08CycleTestByDirectory extends CycleTestByDirectory {
  override def origin: Hint          = Hint(Raml08, Yaml)
  override def target: Hint          = Hint(Raml08, Yaml)
  override def fileExtension: String = ".raml"

  override def basePath: String =
    "amf-cli/shared/src/test/resources/production/raml08/"
}
