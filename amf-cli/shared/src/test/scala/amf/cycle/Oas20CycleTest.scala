package amf.cycle

import amf.core.internal.remote.{Oas20JsonHint, Oas20YamlHint}
import amf.io.FunSuiteCycleTests

class Oas20CycleTest extends FunSuiteCycleTests {

  override def basePath: String = "amf-cli/shared/src/test/resources/upanddown/cycle/oas20/"

  test("Invalid oas type with non-integer minimum doesn't throw exception in emission") {
    cycle(
      "json/invalid-type-with-string-minimum.json",
      "json/invalid-type-with-string-minimum.cycled.json",
      Oas20JsonHint,
      Oas20JsonHint
    )
  }

  test("Emitting required in body") {
    cycle(
      "yaml/required-body-parameter/api.yaml",
      "yaml/required-body-parameter/dumped.yaml",
      Oas20YamlHint,
      Oas20YamlHint
    )
  }

  test("Emitting required in form-data") {
    cycle(
      "yaml/required-field-form-data/api.yaml",
      "yaml/required-field-form-data/dumped.yaml",
      Oas20YamlHint,
      Oas20YamlHint
    )
  }
}
