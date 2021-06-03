package amf.cycle

import amf.core.remote.{Raml, Raml10, Raml10YamlHint}
import amf.io.FunSuiteCycleTests

class YamlSpecCycleTest extends FunSuiteCycleTests {

  override val basePath = "amf-client/shared/src/test/resources/yaml/"

  test("Example 5.3 - Mapping key structure indicator") {
    cycle("example-5.3.raml", "example-5.3.raml.raml", Raml10YamlHint, Raml10)
  }

  test("Example 6.29 - Node anchors") {
    cycle("example-6.29.raml", "example-6.29.raml.raml", Raml10YamlHint, Raml10)
  }

  test("Example 8.7 - Literal scalar") {
    cycle("example-8.7.raml", Raml10YamlHint)
  }
}
