package amf.emit

import amf.core.remote.{Amf, AmfJsonHint, Raml, RamlYamlHint}
import amf.io.BuildCycleTests

class CycleForExternalSource extends BuildCycleTests {

  val jsonPath = "amf-client/shared/src/test/resources/production/financial-api/othercases/jsonschema/"
  val xsdPath  = "amf-client/shared/src/test/resources/production/financial-api/othercases/xsdschema/"

  // todo : rename and do this test ok

  // ----begin temp test ---------------------------------------------------------------------
  test("Test ext ref json schema raml to jsonld") {
    cycle(
      "api.raml",
      "api.raml.jsonld",
      RamlYamlHint,
      Amf,
      jsonPath
    )
  }

  test("Test ext ref json schema jsonld to raml") {
    cycle(
      "api.raml.jsonld",
      "api.raml.jsonld.raml",
      AmfJsonHint,
      Raml,
      jsonPath
    )
  }

  test("Test ext ref json raml to raml") {
    cycle(
      "api.raml",
      "api.raml.jsonld.raml",
      RamlYamlHint,
      Raml,
      jsonPath
    )
  }

  test("Test ext ref xsd schema raml to jsonld") {
    cycle(
      "api.raml",
      "api.raml.jsonld",
      RamlYamlHint,
      Amf,
      xsdPath
    )
  }

  test("Test ext ref xsd schema jsonld to raml") {
    cycle(
      "api.raml.jsonld",
      "api.raml.jsonld.raml",
      AmfJsonHint,
      Raml,
      xsdPath
    )
  }

  test("Test ext ref xsd raml to raml") {
    cycle(
      "api.raml",
      "api.raml.jsonld.raml",
      RamlYamlHint,
      Raml,
      xsdPath
    )
  }

  test("Test ext example xsd raml to jsonld") {
    cycle(
      "api.raml",
      "api.raml.jsonld",
      RamlYamlHint,
      Amf,
      "amf-client/shared/src/test/resources/production/financial-api/othercases/xsdexample/"
    )
  }

  test("Test fragment link shape json raml to jsonld") {
    cycle(
      "api-with-fragment-ref.raml",
      "api-with-fragment-ref.raml.jsonld",
      RamlYamlHint,
      Amf,
      jsonPath
    )
  }

  test("Test fragment link shape json raml to raml") {
    cycle(
      "api-with-fragment-ref.raml",
      "api-with-fragment-ref.raml.raml",
      RamlYamlHint,
      Raml,
      jsonPath
    )
  }

  test("Test fragment link shape xml raml to jsonld") {
    cycle(
      "api-with-fragment-ref-xml.raml",
      "api-with-fragment-ref-xml.raml.jsonld",
      RamlYamlHint,
      Amf,
      xsdPath
    )
  }

  test("Test fragment link shape xml raml to raml") {
    cycle(
      "api-with-fragment-ref-xml.raml",
      "api-with-fragment-ref-xml.raml.raml",
      RamlYamlHint,
      Raml,
      xsdPath
    )
  }
  // ---- end temp test ---------------------------------------------------------------------
  override val basePath: String = ""
}
