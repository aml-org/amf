package amf.cycle

import amf.core.remote.{Raml, RamlYamlHint, Vendor}
import amf.io.FunSuiteCycleTests
import amf.plugins.document.webapi.parser.RamlHeader.Raml10

class EscapeCycleTest extends FunSuiteCycleTests {

  override val basePath = "amf-client/shared/src/test/resources/escape/"

  test("Escape test with problematic characters") {
    cycle("simple-api.raml", RamlYamlHint)
  }

  test("Escape test with problematic characters in value") {
    cycle("multiple-escapes.raml", "multiple-escapes.golden.raml", RamlYamlHint, Raml)
  }
}
