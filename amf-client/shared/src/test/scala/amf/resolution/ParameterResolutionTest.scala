package amf.resolution

import amf.core.remote._
import amf.remote._

class ParameterResolutionTest extends ResolutionTest {

  override val basePath = "amf-client/shared/src/test/resources/resolution/"

  test("resolution AMF") {
    cycle("parameters.raml", "parameters.raml.jsonld", RamlYamlHint, Amf)
  }

  test("resolution OpenAPI") {
    cycle("parameters.json", "parameters.json.jsonld", OasJsonHint, Amf, transformWith = Some(Oas))
  }

  test("nested parameters AMF") {
    cycle("nested-parameters.raml", "nested-parameters.raml.jsonld", RamlYamlHint, Amf)
  }
}
