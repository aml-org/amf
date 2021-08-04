package amf.resolution

import amf.core.client.scala.config.RenderOptions
import amf.core.internal.remote.{Amf, AmfJsonHint, Oas20, Oas20JsonHint, Oas30, Raml10, Raml10YamlHint}

class MediaTypeResolutionTest extends ResolutionTest {
  override val basePath = "amf-cli/shared/src/test/resources/resolution/media-type/"

  multiGoldenTest("One mediaType raml to AMF", "media-type.raml.%s") { config =>
    cycle("media-type.raml",
          config.golden,
          Raml10YamlHint,
          target = AmfJsonHint,
          renderOptions = Some(config.renderOptions),
          transformWith = Some(Raml10))
  }

  multiGoldenTest("Multiple mediaTypes raml to AMF", "media-types.raml.%s") { config =>
    cycle("media-types.raml",
          config.golden,
          Raml10YamlHint,
          target = AmfJsonHint,
          renderOptions = Some(config.renderOptions),
          transformWith = Some(Raml10))
  }

  multiGoldenTest("Override mediaType raml to AMF", "media-type-override.raml.%s") { config =>
    cycle("media-type-override.raml",
          config.golden,
          Raml10YamlHint,
          target = AmfJsonHint,
          renderOptions = Some(config.renderOptions),
          transformWith = Some(Raml10))
  }

  multiGoldenTest("One mediaType oas to AMF", "media-type.json.%s") { config =>
    cycle("media-type.json",
          config.golden,
          Oas20JsonHint,
          target = AmfJsonHint,
          renderOptions = Some(config.renderOptions),
          transformWith = Some(Raml10))
  }

  multiGoldenTest("Multiple mediaTypes oas to AMF", "media-types.json.%s") { config =>
    cycle("media-types.json",
          config.golden,
          Oas20JsonHint,
          target = AmfJsonHint,
          renderOptions = Some(config.renderOptions),
          transformWith = Some(Raml10))
  }

  multiGoldenTest("Override mediaType oas to AMF", "media-type-override.json.%s") { config =>
    cycle("media-type-override.json",
          config.golden,
          Oas20JsonHint,
          target = AmfJsonHint,
          renderOptions = Some(config.renderOptions),
          transformWith = Some(Raml10))
  }

  // Different target, should keep accepts and consumes fields as they are required in OAS.
  multiGoldenTest("Override mediaType oas to OAS", "media-type-override-oas-target.json.%s") { config =>
    cycle("media-type-override.json",
          config.golden,
          Oas20JsonHint,
          target = AmfJsonHint,
          renderOptions = Some(config.renderOptions),
          transformWith = Some(Oas20))
  }

  override def defaultRenderOptions: RenderOptions = RenderOptions().withSourceMaps.withPrettyPrint
}
