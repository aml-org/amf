package amf.resolution

import amf.core.client.scala.config.RenderOptions
import amf.core.internal.remote.{Amf, Oas20JsonHint, Raml10, Raml10YamlHint}
import amf.testing.AmfJsonLd

/**
  *
  */
class QueryStringResolutionTest extends ResolutionTest {
  override val basePath = "amf-cli/shared/src/test/resources/resolution/queryString/"

  multiGoldenTest("QueryString raml to AMF", "query-string.raml.%s") { config =>
    cycle("query-string.raml",
          config.golden,
          Raml10YamlHint,
          target = AmfJsonLd,
          renderOptions = Some(config.renderOptions),
          transformWith = Some(Raml10))
  }

  multiGoldenTest("QueryString oas to AMF", "query-string.json.%s") { config =>
    cycle("query-string.json",
          config.golden,
          Oas20JsonHint,
          target = AmfJsonLd,
          renderOptions = Some(config.renderOptions),
          transformWith = Some(Raml10))
  }

  multiGoldenTest("Security Scheme with Query String oas to AMF", "security-with-query-string.json.%s") { config =>
    cycle("security-with-query-string.json",
          config.golden,
          Oas20JsonHint,
          target = AmfJsonLd,
          renderOptions = Some(config.renderOptions),
          transformWith = Some(Raml10))
  }

  multiGoldenTest("Security Scheme with Query String raml to AMF", "security-with-query-string.raml.%s") { config =>
    cycle("security-with-query-string.raml",
          config.golden,
          Raml10YamlHint,
          target = AmfJsonLd,
          renderOptions = Some(config.renderOptions),
          transformWith = Some(Raml10))
  }

  override def defaultRenderOptions: RenderOptions = RenderOptions().withSourceMaps.withPrettyPrint
}
