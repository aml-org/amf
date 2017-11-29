package amf.resolution

import amf.core.remote.{Amf, OasJsonHint, RamlYamlHint}

class SecurityResolutionTest extends ResolutionTest {

  override val basePath = "amf-client/shared/src/test/resources/resolution/security/"

  test("Security resolution raml to AMF") {
    cycle("security.raml", "security.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Security resolution oas to AMF") {
    cycle("security.json", "security.json.jsonld", OasJsonHint, Amf)
  }
}
