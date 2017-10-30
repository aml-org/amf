package amf.resolution

import amf.remote.{Amf, OasJsonHint, RamlYamlHint}

/**
  *
  */
class ExamplesResolutionTest extends ResolutionTest {
  override val basePath: String = "file://shared/src/test/resources/resolution/examples/"

  test("Response examples oas to AMF") {
    cycle("response-examples.json", "response-examples.json.jsonld", OasJsonHint, Amf)
  }

  test("Response examples raml to AMF") {
    cycle("response-examples.raml", "response-examples.raml.jsonld", RamlYamlHint, Amf)
  }
}
