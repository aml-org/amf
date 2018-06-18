package amf.cycle

import amf.core.remote.{Raml, RamlYamlHint}
import amf.io.BuildCycleTests

class EscapeCycleTest extends BuildCycleTests {

  override val basePath = "parser-client/shared/src/test/resources/escape/"

  test("Escape test with problematic characters") {
    cycle("simple-api.raml", RamlYamlHint)
  }
}
