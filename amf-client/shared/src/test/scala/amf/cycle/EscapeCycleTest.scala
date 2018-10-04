package amf.cycle

import amf.core.remote.{Raml, RamlYamlHint}
import amf.io.{BuildCycleTests, FunSuiteCycleTests}

class EscapeCycleTest extends FunSuiteCycleTests {

  override val basePath = "amf-client/shared/src/test/resources/escape/"

  test("Escape test with problematic characters") {
    cycle("simple-api.raml", RamlYamlHint)
  }
}
