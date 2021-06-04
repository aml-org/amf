package amf.validation

import amf.client.environment.AMFConfiguration
import amf.client.remod.amfcore.config.RenderOptions
import amf.client.remod.amfcore.resolution.PipelineName
import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.remote._
import amf.core.resolution.pipelines.{TransformationPipeline, TransformationPipelineRunner}
import amf.core.services.RuntimeResolver
import amf.io.FunSuiteCycleTests
import amf.plugins.document.apicontract.resolution.pipelines.AmfEditingPipeline

class JapaneseResolvedCycleTest extends FunSuiteCycleTests {

  override def basePath = "amf-cli/shared/src/test/resources/validations/japanese/resolve/"

  multiGoldenTest("Raml10 to Json-LD resolves", "ramlapi.%s") { config =>
    cycle("ramlapi.raml", config.golden, Raml10YamlHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  multiSourceTest("Flattened Json-LD resolves to Raml", "ramlapi.%s") { config =>
    cycle(config.source, "resolved-ramlapi.raml", AmfJsonHint, Raml10)
  }

  multiGoldenTest("Oas20 to Json-LD resolves", "oasapi.%s") { config =>
    cycle("oasapi.json", config.golden, Oas20YamlHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  multiGoldenTest("Oas30 to JSON-LD resolves", "oas30api.%s") { config =>
    cycle("oas30api.json", config.golden, Oas30YamlHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  test("RAML emission applies singularize") {
    cycle("singularize.raml", "resolved-singularize.raml", Raml10YamlHint, Raml10)
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

  override def transform(unit: BaseUnit, config: CycleConfig, amfConfig: AMFConfiguration): BaseUnit =
    config.target match {
      case Raml08 | Raml10 | Oas20 | Oas30 =>
        amfConfig
          .withErrorHandlerProvider(() => UnhandledErrorHandler)
          .createClient()
          .transform(unit, PipelineName.from(config.target.name, TransformationPipeline.EDITING_PIPELINE))
          .bu
      case Amf    => TransformationPipelineRunner(UnhandledErrorHandler).run(unit, AmfEditingPipeline())
      case target => throw new Exception(s"Cannot resolve $target")
    }
}
