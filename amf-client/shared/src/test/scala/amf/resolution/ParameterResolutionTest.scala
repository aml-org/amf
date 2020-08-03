package amf.resolution

import amf.core.emitter.RenderOptions
import amf.core.remote._
import amf.remote._

class ParameterResolutionTest extends ResolutionTest {

  override val basePath = "amf-client/shared/src/test/resources/resolution/"

  multiGoldenTest("resolution AMF", "parameters.raml.%s") { config =>
    cycle("parameters.raml", config.golden, RamlYamlHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("resolution OpenAPI", "parameters.json.%s") { config =>
    cycle("parameters.json",
          config.golden,
          OasJsonHint,
          target = Amf,
          renderOptions = Some(config.renderOptions),
          transformWith = Some(Oas))
  }

  multiGoldenTest("nested parameters AMF", "nested-parameters.raml.%s") { config =>
    cycle("nested-parameters.raml",
          config.golden,
          RamlYamlHint,
          target = Amf,
          renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("RAML 0.8 overrided baseUriParams are propagated to request", "overrided-baseUriParams.%s") {
    config =>
      cycle("overrided-baseUriParams.raml",
            config.golden,
            RamlYamlHint,
            target = Amf,
            renderOptions = Some(config.renderOptions),
            transformWith = Some(Raml08))
  }

  override def defaultRenderOptions: RenderOptions = RenderOptions().withSourceMaps.withPrettyPrint
}
