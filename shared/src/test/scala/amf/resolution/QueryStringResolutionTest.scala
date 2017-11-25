package amf.resolution

import amf.core.remote.{Amf, OasJsonHint, RamlYamlHint}

/**
  *
  */
class QueryStringResolutionTest extends ResolutionTest {
  override val basePath = "shared/src/test/resources/resolution/queryString/"

  test("QueryString raml to AMF") {
    cycle("query-string.raml", "query-string.raml.jsonld", RamlYamlHint, Amf)
  }

  test("QueryString oas to AMF") {
    cycle("query-string.json", "query-string.json.jsonld", OasJsonHint, Amf)
  }

  test("Security Scheme with Query String oas to AMF") {
    cycle("security-with-query-string.json", "security-with-query-string.json.jsonld", OasJsonHint, Amf)
  }

  test("Security Scheme with Query String raml to AMF") {
    cycle("security-with-query-string.raml", "security-with-query-string.raml.jsonld", RamlYamlHint, Amf)
  }
}
