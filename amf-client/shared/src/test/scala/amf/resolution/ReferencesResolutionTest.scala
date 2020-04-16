package amf.resolution

import amf.core.remote.{Amf, RamlYamlHint}

class ReferencesResolutionTest extends ResolutionTest {
  override val basePath: String = "amf-client/shared/src/test/resources/upanddown/"

  test("References resolution") {
    cycle("with_references.raml", "with_references_resolved.jsonld", RamlYamlHint, Amf)
  }
}
