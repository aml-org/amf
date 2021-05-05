package amf.validation

import amf.core.emitter.RenderOptions
import amf.core.remote._
import amf.io.FunSuiteCycleTests

class JapaneseCycleTest extends FunSuiteCycleTests {

  override def basePath = "amf-client/shared/src/test/resources/validations/japanese/cycle/"

  multiGoldenTest("Raml10 to Json-LD resolves", "ramlapi.%s") { config =>
    cycle("ramlapi.raml", config.golden, Raml10YamlHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  multiSourceTest("Json-LD resolves to Raml10", "ramlapi.%s") { config =>
    cycle(config.source, "cycled-ramlapi.raml", AmfJsonHint, Raml10)
  }

  multiGoldenTest("Oas20 to Json-LD resolves", "oasapi.%s") { config =>
    cycle("oasapi.json", config.golden, Oas20YamlHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Oas30 to JSON-LD resolves", "oas30api.%s") { config =>
    cycle("oas30api.json", config.golden, Oas30YamlHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

// TODO: JSON-LD to OAS doesnt decode Japanese characters. It keeps them encoded. RAML does.

  multiSourceTest("Json-LD resolves to OAS20", "oasapi.%s") { config =>
    cycle(config.source, "cycled-oasapi.json", AmfJsonHint, Oas20)
  }

  multiSourceTest("Json-LD resolves to OAS30", "oas30api.%s") { config =>
    cycle(config.source, "cycled-oas30api.json", AmfJsonHint, Oas30)
  }

  override def defaultRenderOptions: RenderOptions = RenderOptions().withSourceMaps.withPrettyPrint
}
