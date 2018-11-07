package amf.cycle

import amf.core.remote.{Hint, OasJsonHint, OasYamlHint, RamlYamlHint}

/**
  * Cycle by directory test for dir: [[amf-client/shared/src/test/resources/upanddown/cycle/oas20/yaml]]
  *   origin: Oas20
  *   target: Oas20
  */
class Oas20YamlCycleTestByDirectory extends CycleTestByDirectory {
  override def origin: Hint          = OasYamlHint
  override def target: Hint          = OasYamlHint
  override def fileExtension: String = ".yaml"

  override lazy val withEnableValidations: Seq[String] = Seq("multiple-form-data", "invalid-param-ref")

  override def basePath: String =
    "amf-client/shared/src/test/resources/upanddown/cycle/oas20/yaml/" // todo: move one level up
}

/**
  * Cycle by directory test for dir: [[amf-client/shared/src/test/resources/upanddown/cycle/oas20/json]]
  *   origin: Oas20
  *   target: Oas20
  */
class Oas20JsonCycleTestByDirectory extends CycleTestByDirectory {
  override def origin: Hint          = OasJsonHint
  override def target: Hint          = OasJsonHint
  override def fileExtension: String = ".json"

  override def basePath: String =
    "amf-client/shared/src/test/resources/upanddown/cycle/oas20/json/" // todo: move one level up
}
