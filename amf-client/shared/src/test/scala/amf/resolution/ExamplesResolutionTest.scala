package amf.resolution

import amf.core.remote.{Amf, OasJsonHint, OasYamlHint, RamlYamlHint}

/**
  *
  */
class ExamplesResolutionTest extends ResolutionTest {
  override val basePath      = "amf-client/shared/src/test/resources/resolution/examples/"
  val validationPath: String = "amf-client/shared/src/test/resources/validations/"

  test("Response examples oas to AMF") {
    cycle("response-examples.json", "response-examples.json.jsonld", OasJsonHint, Amf)
  }

  test("Response declarations with multiple media types oas to AMF") {
    cycle("response-declarations-with-multiple-media-types.yaml",
          "response-declarations-with-multiple-media-types.jsonld",
          OasYamlHint,
          Amf)
  }

  test("Response examples raml to AMF") {
    cycle("response-examples.raml", "response-examples.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Typed external fragment as included example - Vocabulary") {
    cycle("examples/vocabulary-fragment/api.raml",
          "examples/vocabulary-fragment/api.jsonld",
          RamlYamlHint,
          Amf,
          validationPath)
  }

  test("Typed external fragment as included example - Dialect") {
    cycle("examples/dialect-fragment/api.raml",
          "examples/dialect-fragment/api.jsonld",
          RamlYamlHint,
          Amf,
          validationPath)
  }
}
