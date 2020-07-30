package amf.validation

import amf.core.emitter.RenderOptions
import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.remote._
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.io.FunSuiteCycleTests
import amf.plugins.document.webapi.resolution.pipelines.AmfEditingPipeline
import amf.plugins.document.webapi.{Oas20Plugin, Oas30Plugin, Raml08Plugin, Raml10Plugin}

class JapaneseResolvedCycleTest extends FunSuiteCycleTests {

  override def basePath = "amf-client/shared/src/test/resources/validations/japanese/resolve/"

  multiGoldenTest("Raml10 to Json-LD resolves", "ramlapi.%s") { config =>
    cycle("ramlapi.raml", config.golden, RamlYamlHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  multiSourceTest("Flattened Json-LD resolves to Raml", "ramlapi.%s") { config =>
    cycle(config.source, "resolved-ramlapi.raml", AmfJsonHint, Raml10)
  }

  multiGoldenTest("Oas20 to Json-LD resolves", "oasapi.%s") { config =>
    cycle("oasapi.json", config.golden, OasYamlHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Oas30 to JSON-LD resolves", "oas30api.%s") { config =>
    cycle("oas30api.json", config.golden, OasYamlHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  test("RAML emission applies singularize") {
    cycle("singularize.raml", "resolved-singularize.raml", RamlYamlHint, Raml10)
  }

// TODO: JSON-LD to OAS doesnt decode Japanese characters. RAML does
  /*
  test("Json-LD resolves to OAS20") {
    cycle("oasapi.jsonld", "resolved-oasapi.json", AmfJsonHint, Oas20)
  }

  test("JSON-LD to OAS30 resolves") {
    cycle("oas30api.jsonld", "resolved-oas30api.json", AmfJsonHint, Oas30)
  }
   */

  override def defaultRenderOptions: RenderOptions = RenderOptions().withSourceMaps.withPrettyPrint

  override def transform(unit: BaseUnit, config: CycleConfig): BaseUnit =
    config.target match {
      case Raml08        => Raml08Plugin.resolve(unit, UnhandledErrorHandler, ResolutionPipeline.EDITING_PIPELINE)
      case Raml | Raml10 => Raml10Plugin.resolve(unit, UnhandledErrorHandler, ResolutionPipeline.EDITING_PIPELINE)
      case Oas30         => Oas30Plugin.resolve(unit, UnhandledErrorHandler, ResolutionPipeline.EDITING_PIPELINE)
      case Oas | Oas20   => Oas20Plugin.resolve(unit, UnhandledErrorHandler, ResolutionPipeline.EDITING_PIPELINE)
      case Amf           => AmfEditingPipeline.unhandled.resolve(unit)
      case target        => throw new Exception(s"Cannot resolve $target")
    }
}
