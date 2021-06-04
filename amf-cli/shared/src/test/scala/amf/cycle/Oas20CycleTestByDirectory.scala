package amf.cycle

import amf.core.remote.{Hint, Oas20JsonHint, Oas20YamlHint}

/**
  * Cycle by directory test for dir: [[amf-cli/shared/src/test/resources/upanddown/cycle/oas20/yaml]]
  *   origin: Oas20
  *   target: Oas20
  */
class Oas20YamlCycleTestByDirectory extends CycleTestByDirectory {
  override def origin: Hint          = Oas20YamlHint
  override def target: Hint          = Oas20YamlHint
  override def fileExtension: String = ".yaml"

  override lazy val withEnableValidations: Seq[String] = Seq("multiple-form-data", "invalid-param-ref")

  override def basePath: String =
    "amf-cli/shared/src/test/resources/upanddown/cycle/oas20/yaml/" // todo: move one level up
}

/**
  * Cycle by directory test for dir: [[amf-cli/shared/src/test/resources/upanddown/cycle/oas20/json]]
  *   origin: Oas20
  *   target: Oas20
  */
class Oas20JsonCycleTestByDirectory extends CycleTestByDirectory {
  override def origin: Hint          = Oas20JsonHint
  override def target: Hint          = Oas20JsonHint
  override def fileExtension: String = ".json"

  override def basePath: String =
    "amf-cli/shared/src/test/resources/upanddown/cycle/oas20/json/" // todo: move one level up
}
