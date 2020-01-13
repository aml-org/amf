package amf.validation

import amf.core.remote._
import amf.io.FunSuiteCycleTests

class JapaneseCycleTest extends FunSuiteCycleTests {

  override def basePath = "amf-client/shared/src/test/resources/validations/japanese/cycle/"

  test("Raml10 to Json-LD resolves") {
    cycle("ramlapi.raml", "ramlapi.jsonld", RamlYamlHint, Amf)
  }

  test("Json-LD resolves to Raml10") {
    cycle("ramlapi.jsonld", "cycled-ramlapi.raml", AmfJsonHint, Raml10)
  }

  test("Oas20 to Json-LD resolves") {
    cycle("oasapi.json", "oasapi.jsonld", OasYamlHint, Amf)
  }

  test("Oas30 to JSON-LD resolves") {
    cycle("oas30api.json", "oas30api.jsonld", OasYamlHint, Amf)
  }

// TODO: JSON-LD to OAS doesnt decode Japanese characters. It keeps them encoded. RAML does.

  test("Json-LD resolves to OAS20") {
    cycle("oasapi.jsonld", "cycled-oasapi.json", AmfJsonHint, Oas20)
  }

  test("Json-LD resolves to OAS30") {
    cycle("oas30api.jsonld", "cycled-oas30api.json", AmfJsonHint, Oas30)
  }
}
