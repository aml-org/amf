package amf.cycle

import amf.core.internal.remote.{Raml10, Raml10YamlHint}
import amf.io.FunSuiteCycleTests

class EscapeCycleTest extends FunSuiteCycleTests {

  override val basePath = "amf-cli/shared/src/test/resources/escape/"

  test("Escape test with problematic characters") {
    cycle("simple-api.raml", Raml10YamlHint)
  }

  test("Escape test with problematic characters in value") {
    cycle("multiple-escapes.raml", "multiple-escapes.golden.raml", Raml10YamlHint, Raml10)
  }
}
