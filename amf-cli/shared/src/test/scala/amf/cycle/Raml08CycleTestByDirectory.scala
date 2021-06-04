package amf.cycle

import amf.core.remote.Syntax.Yaml
import amf.core.remote.{Hint, Raml08}

/**
  * Cycle by directory test for dir: [[amf-cli/shared/src/test/resources/upanddown/cycle/raml08/]]
  *   origin: Raml08
  *   target: Raml08
  */
class Raml08CycleTestByDirectory extends CycleTestByDirectory {
  override def origin: Hint          = Hint(Raml08, Yaml)
  override def target: Hint          = Hint(Raml08, Yaml)
  override def fileExtension: String = ".raml"

  override def basePath: String =
    "amf-cli/shared/src/test/resources/upanddown/cycle/raml08/" // todo: move one level up
}
