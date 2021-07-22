package amf.cycle

import amf.core.internal.remote.{Raml10, Raml10YamlHint}
import amf.io.FunSuiteCycleTests
import amf.testing.Raml10Yaml

class YamlSpecCycleTest extends FunSuiteCycleTests {

  override val basePath = "amf-cli/shared/src/test/resources/yaml/"

  test("Example 5.3 - Mapping key structure indicator") {
    cycle("example-5.3.raml", "example-5.3.raml.raml", Raml10YamlHint, Raml10Yaml)
  }

  test("Example 6.29 - Node anchors") {
    cycle("example-6.29.raml", "example-6.29.raml.raml", Raml10YamlHint, Raml10Yaml)
  }

  test("Example 8.7 - Literal scalar") {
    cycle("example-8.7.raml", Raml10YamlHint)
  }
}
