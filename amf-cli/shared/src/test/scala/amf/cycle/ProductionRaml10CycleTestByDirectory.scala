package amf.cycle

import amf.core.remote.{Hint, Raml10YamlHint}

/**
  * Cycle by directory test for dir: [[amf-client/shared/src/test/resources/production/raml10/]]
  *   origin: Raml10
  *   target: Raml10
  */
class ProductionRaml10CycleTestByDirectory extends CycleTestByDirectory {
  override def origin: Hint          = Raml10YamlHint
  override def target: Hint          = Raml10YamlHint
  override def fileExtension: String = ".raml"

  override def basePath: String =
    "amf-client/shared/src/test/resources/production/raml10/"
}
