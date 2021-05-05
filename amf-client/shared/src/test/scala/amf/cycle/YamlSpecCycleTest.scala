package amf.cycle

import amf.core.remote.{Raml, RamlYamlHint}
import amf.io.{BuildCycleTests, FunSuiteCycleTests}

class YamlSpecCycleTest extends FunSuiteCycleTests {

  override val basePath = "amf-client/shared/src/test/resources/yaml/"

  test("Example 5.3 - Mapping key structure indicator") {
    cycle("example-5.3.raml", "example-5.3.raml.raml", RamlYamlHint, Raml)
  }

  test("Example 6.29 - Node anchors") {
    cycle("example-6.29.raml", "example-6.29.raml.raml", RamlYamlHint, Raml)
  }

  test("Example 8.7 - Literal scalar") {
    cycle("example-8.7.raml", RamlYamlHint)
  }
}
