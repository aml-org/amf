package amf.cycle

import amf.core.remote.{Hint, OasYamlHint, RamlYamlHint}

/**
  * Cycle by directory test for dir: [[amf-client/shared/src/test/resources/upanddown/cycle/oas20/]]
  *   origin: Oas20
  *   target: Oas20
  */
class Oas20CycleTestByDirectory extends CycleTestByDirectory {
  override def origin: Hint          = OasYamlHint
  override def target: Hint          = OasYamlHint
  override def fileExtension: String = ".yaml"

  override lazy val withEnableValidations: Seq[String] = Seq("multiple-form-data")

  override def basePath: String =
    "amf-client/shared/src/test/resources/upanddown/cycle/oas20/" // todo: move one level up
}
