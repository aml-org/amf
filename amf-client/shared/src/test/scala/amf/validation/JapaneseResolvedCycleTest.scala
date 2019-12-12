package amf.validation

import amf.core.model.document.BaseUnit
import amf.core.parser.UnhandledErrorHandler
import amf.core.remote._
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.io.FunSuiteCycleTests
import amf.plugins.document.webapi.resolution.pipelines.AmfEditingPipeline
import amf.plugins.document.webapi.{Oas20Plugin, Oas30Plugin, Raml08Plugin, Raml10Plugin}

class JapaneseResolvedCycleTest extends FunSuiteCycleTests {

  override def basePath = "amf-client/shared/src/test/resources/validations/japanese/resolve/"

  test("Raml10 to Json-LD resolves") {
    cycle("ramlapi.raml", "ramlapi.jsonld", RamlYamlHint, Amf)
  }

  test("Json-LD resolves to Raml") {
    cycle("ramlapi.jsonld", "resolved-ramlapi.raml", AmfJsonHint, Raml10)
  }

  test("Oas20 to Json-LD resolves") {
    cycle("oasapi.json", "oasapi.jsonld", OasYamlHint, Amf)
  }

  test("Oas30 to JSON-LD resolves") {
    cycle("oas30api.json", "oas30api.jsonld", OasYamlHint, Amf)
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
