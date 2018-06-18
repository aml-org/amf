package amf.resolution

import amf.core.remote.{Amf, RamlYamlHint}

/**
  *
  */
class ExternalSourceResolutionTest extends ResolutionTest {
  override val basePath = "parser-client/shared/src/test/resources/resolution/externalfragment/"

  test("Xml schema raml to amf") {
    cycle("xmlschema.raml", "xmlschema.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Json schema raml to amf") {
    cycle("jsonschema.raml", "jsonschema.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Xml example raml to amf") {
    cycle("xmlexample.raml", "xmlexample.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Json example raml to amf") {
    cycle("jsonexample.raml", "jsonexample.raml.jsonld", RamlYamlHint, Amf)
  }
}
