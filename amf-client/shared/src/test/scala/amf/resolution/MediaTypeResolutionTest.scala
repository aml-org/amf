package amf.resolution

import amf.core.emitter.RenderOptions
import amf.core.remote.{Amf, Oas, OasJsonHint, RamlYamlHint}

class MediaTypeResolutionTest extends ResolutionTest {
  override val basePath = "amf-client/shared/src/test/resources/resolution/media-type/"

  multiGoldenTest("One mediaType raml to AMF", "media-type.raml.%s") { config =>
    cycle("media-type.raml", config.golden, RamlYamlHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Multiple mediaTypes raml to AMF", "media-types.raml.%s") { config =>
    cycle("media-types.raml", config.golden, RamlYamlHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Override mediaType raml to AMF", "media-type-override.raml.%s") { config =>
    cycle("media-type-override.raml",
          config.golden,
          RamlYamlHint,
          target = Amf,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("One mediaType oas to AMF", "media-type.json.%s") { config =>
    cycle("media-type.json", config.golden, OasJsonHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Multiple mediaTypes oas to AMF", "media-types.json.%s") { config =>
    cycle("media-types.json", config.golden, OasJsonHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Override mediaType oas to AMF", "media-type-override.json.%s") { config =>
    cycle("media-type-override.json",
          config.golden,
          OasJsonHint,
          target = Amf,
          renderOptions = Some(config.renderOptions))
  }

  // Different target, should keep accepts and consumes fields as they are required in OAS.
  multiGoldenTest("Override mediaType oas to OAS", "media-type-override-oas-target.json.%s") { config =>
    cycle("media-type-override.json",
          config.golden,
          OasJsonHint,
          target = Amf,
          renderOptions = Some(config.renderOptions),
          transformWith = Some(Oas))
  }

  override def defaultRenderOptions: RenderOptions = RenderOptions().withSourceMaps.withPrettyPrint
}
