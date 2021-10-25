package amf.pce

import amf.core.remote.{Amf, RamlYamlHint}
import amf.io.FunSuiteCycleTests

class PceJsonLdTest extends FunSuiteCycleTests {
  override def basePath: String = "amf-client/shared/src/test/resources/pce/"

  test("Test schema dependencies JSON-LD serialization") {
    cycle("valid-schema-dependencies.raml", "valid-schema-dependencies.jsonld", RamlYamlHint, Amf)
  }
}
