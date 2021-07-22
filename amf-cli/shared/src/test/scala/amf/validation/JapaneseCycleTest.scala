package amf.validation

import amf.core.client.scala.config.RenderOptions
import amf.core.internal.remote.Syntax.Json
import amf.core.internal.remote.{Amf, AmfJsonHint, Oas20, Oas20YamlHint, Oas30, Oas30YamlHint, Raml10, Raml10YamlHint}
import amf.io.FunSuiteCycleTests
import amf.testing.{AmfJsonLd, Oas20Json, Oas30Json, Raml10Yaml}

class JapaneseCycleTest extends FunSuiteCycleTests {

  override def basePath = "amf-cli/shared/src/test/resources/validations/japanese/cycle/"

  multiGoldenTest("Raml10 to Json-LD resolves", "ramlapi.%s") { config =>
    cycle("ramlapi.raml",
          config.golden,
          Raml10YamlHint,
          target = AmfJsonLd,
          renderOptions = Some(config.renderOptions))
  }

  multiSourceTest("Json-LD resolves to Raml10", "ramlapi.%s") { config =>
    cycle(config.source, "cycled-ramlapi.raml", AmfJsonHint, Raml10Yaml)
  }

  multiGoldenTest("Oas20 to Json-LD resolves", "oasapi.%s") { config =>
    cycle("oasapi.json", config.golden, Oas20YamlHint, target = AmfJsonLd, renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Oas30 to JSON-LD resolves", "oas30api.%s") { config =>
    cycle("oas30api.json",
          config.golden,
          Oas30YamlHint,
          target = AmfJsonLd,
          renderOptions = Some(config.renderOptions))
  }

// TODO: JSON-LD to OAS doesnt decode Japanese characters. It keeps them encoded. RAML does.

  multiSourceTest("Json-LD resolves to OAS20", "oasapi.%s") { config =>
    cycle(config.source, "cycled-oasapi.json", AmfJsonHint, Oas20Json)
  }

  multiSourceTest("Json-LD resolves to OAS30", "oas30api.%s") { config =>
    cycle(config.source, "cycled-oas30api.json", AmfJsonHint, Oas30Json, syntax = Some(Json))
  }

  override def defaultRenderOptions: RenderOptions = RenderOptions().withSourceMaps.withPrettyPrint
}
