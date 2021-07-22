package amf.resolution

import amf.core.client.scala.config.RenderOptions
import amf.core.internal.remote._
import amf.testing.AmfJsonLd

class ParameterResolutionTest extends ResolutionTest {

  override val basePath = "amf-cli/shared/src/test/resources/resolution/"

  multiGoldenTest("resolution AMF", "parameters.raml.%s") { config =>
    cycle("parameters.raml",
          config.golden,
          Raml10YamlHint,
          target = AmfJsonLd,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("resolution OpenAPI", "parameters.json.%s") { config =>
    cycle("parameters.json",
          config.golden,
          Oas20JsonHint,
          target = AmfJsonLd,
          renderOptions = Some(config.renderOptions),
          transformWith = Some(Oas20))
  }

  multiGoldenTest("nested parameters AMF", "nested-parameters.raml.%s") { config =>
    cycle("nested-parameters.raml",
          config.golden,
          Raml10YamlHint,
          target = AmfJsonLd,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("RAML 0.8 overrided baseUriParams are propagated to request", "overrided-baseUriParams.%s") {
    config =>
      cycle("overrided-baseUriParams.raml",
            config.golden,
            Raml08YamlHint,
            target = AmfJsonLd,
            renderOptions = Some(config.renderOptions),
            transformWith = Some(Raml08))
  }

  override def defaultRenderOptions: RenderOptions = RenderOptions().withSourceMaps.withPrettyPrint
}
