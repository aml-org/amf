package amf.resolution

import amf.core.emitter.RenderOptions
import amf.core.remote.{Amf, OasJsonHint, RamlYamlHint}

/**
  *
  */
class QueryStringResolutionTest extends ResolutionTest {
  override val basePath = "amf-client/shared/src/test/resources/resolution/queryString/"

  multiGoldenTest("QueryString raml to AMF", "query-string.raml.%s") { config =>
    cycle("query-string.raml", config.golden, RamlYamlHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("QueryString oas to AMF", "query-string.json.%s") { config =>
    cycle("query-string.json", config.golden, OasJsonHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Security Scheme with Query String oas to AMF", "security-with-query-string.json.%s") { config =>
    cycle("security-with-query-string.json",
          config.golden,
          OasJsonHint,
          target = Amf,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Security Scheme with Query String raml to AMF", "security-with-query-string.raml.%s") { config =>
    cycle("security-with-query-string.raml",
          config.golden,
          RamlYamlHint,
          target = Amf,
          renderOptions = Some(config.renderOptions))
  }

  override def defaultRenderOptions: RenderOptions = RenderOptions().withSourceMaps.withPrettyPrint
}
