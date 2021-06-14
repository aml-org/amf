package amf.cycle

import amf.core.internal.remote.Syntax.Yaml
import amf.core.internal.remote.{Hint, Raml10YamlHint}

/**
  * Cycle by directory test for dir: [[amf-cli/shared/src/test/resources/production/raml10/]]
  *   origin: Raml10
  *   target: Raml10
  */
class ProductionRaml10CycleTestByDirectory extends CycleTestByDirectory {
  override def origin: Hint          = Raml10YamlHint
  override def target: Hint          = Raml10YamlHint
  override def fileExtension: String = ".raml"

  override def basePath: String =
    "amf-cli/shared/src/test/resources/production/raml10/"
}
