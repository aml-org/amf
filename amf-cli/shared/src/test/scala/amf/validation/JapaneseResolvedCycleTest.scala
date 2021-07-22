package amf.validation

import amf.apicontract.client.scala.AMFConfiguration
import amf.apicontract.internal.transformation.AmfEditingPipeline
import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.transform.TransformationPipelineRunner
import amf.core.internal.remote.{
  Amf,
  AmfJsonHint,
  Oas20,
  Oas20YamlHint,
  Oas30,
  Oas30YamlHint,
  Raml08,
  Raml10,
  Raml10YamlHint
}
import amf.io.FunSuiteCycleTests
import amf.testing.{AmfJsonLd, Raml10Yaml}

class JapaneseResolvedCycleTest extends FunSuiteCycleTests {

  override def basePath = "amf-cli/shared/src/test/resources/validations/japanese/resolve/"

  multiGoldenTest("Raml10 to Json-LD resolves", "ramlapi.%s") { config =>
    cycle("ramlapi.raml",
          config.golden,
          Raml10YamlHint,
          target = AmfJsonLd,
          renderOptions = Some(config.renderOptions))
  }

  multiSourceTest("Flattened Json-LD resolves to Raml", "ramlapi.%s") { config =>
    cycle(config.source, "resolved-ramlapi.raml", AmfJsonHint, Raml10Yaml)
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

  test("RAML emission applies singularize") {
    cycle("singularize.raml", "resolved-singularize.raml", Raml10YamlHint, Raml10Yaml)
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
    config.renderTarget.spec match {
      case Raml08 | Raml10 | Oas20 | Oas30 =>
        amfConfig
          .withErrorHandlerProvider(() => UnhandledErrorHandler)
          .baseUnitClient()
          .transform(unit, PipelineId.Editing)
          .baseUnit
      case Amf    => TransformationPipelineRunner(UnhandledErrorHandler).run(unit, AmfEditingPipeline())
      case target => throw new Exception(s"Cannot resolve $target")
    }
}
