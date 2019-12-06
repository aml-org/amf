package amf.validation

import amf.core.remote.{Amf, AmfJsonHint, Oas20, Oas30, OasYamlHint, Raml10, RamlYamlHint}
import amf.core.remote.Syntax.Yaml
import amf.io.FunSuiteCycleTests

class JapaneseCycleTest extends FunSuiteCycleTests {

  override def basePath = "amf-client/shared/src/test/resources/validations/japanese/resolve/"

  test("Raml10 to Json-LD resolves") {
    cycle("ramlapi.raml", "ramlapi.jsonld", RamlYamlHint, Amf)
  }

  test("Json-LD resolves to Raml") {
    cycle("ramlapi.jsonld", "resolved_ramlapi.raml", AmfJsonHint, Raml10)
  }

  test("Oas20 to Json-LD resolves") {
    cycle("oasapi.json", "oasapi.jsonld", OasYamlHint, Amf)
  }

  // TODO: JSON-LD to OAS doesnt decode Japanese characters. It keeps them encoded. RAML does.
  /*test("Json-LD resolves to Raml10"){
    cycle("oasapi.jsonld", "resolved_oasapi.json", AmfJsonHint, Oas20)
  }*/

  test("Oas30 to JSON-LD resolves") {
    cycle("oas30api.json", "oas30api.jsonld", OasYamlHint, Amf)
  }

  /*test("JSON-LD to OAS30 resolves") {
    cycle("oas30api.jsonld", "resolved_oas30api.json", AmfJsonHint, Oas30)
  }*/
}
