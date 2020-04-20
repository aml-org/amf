package amf.resolution

import amf.core.remote.{Amf, Oas, OasJsonHint, RamlYamlHint}
import amf.remote._

class MediaTypeResolutionTest extends ResolutionTest {
  override val basePath = "amf-client/shared/src/test/resources/resolution/media-type/"

  test("One mediaType raml to AMF") {
    cycle("media-type.raml", "media-type.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Multiple mediaTypes raml to AMF") {
    cycle("media-types.raml", "media-types.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Override mediaType raml to AMF") {
    cycle("media-type-override.raml", "media-type-override.raml.jsonld", RamlYamlHint, Amf)
  }

  test("One mediaType oas to AMF") {
    cycle("media-type.json", "media-type.json.jsonld", OasJsonHint, Amf)
  }

  test("Multiple mediaTypes oas to AMF") {
    cycle("media-types.json", "media-types.json.jsonld", OasJsonHint, Amf)
  }

  test("Override mediaType oas to AMF") {
    cycle("media-type-override.json", "media-type-override.json.jsonld", OasJsonHint, Amf)
  }

  // Different target, should keep accepts and consumes fields as they are required in OAS.
  test("Override mediaType oas to OAS") {
    cycle("media-type-override.json",
          "media-type-override-oas-target.json.jsonld",
          OasJsonHint,
          Amf,
          transformWith = Some(Oas))
  }
}
