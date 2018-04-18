package amf.resolution

import amf.core.benchmark.ExecutionLog
import amf.core.emitter.RenderOptions
import amf.core.model.document.BaseUnit
import amf.core.remote._
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.facades.AMFRenderer
import amf.io.BuildCycleTests
import amf.plugins.document.webapi.resolution.pipelines.AmfEditingPipeline
import amf.plugins.document.webapi.{OAS20Plugin, OAS30Plugin, RAML08Plugin, RAML10Plugin}

import scala.concurrent.{ExecutionContext, Future}

class EditingResolutionTest extends BuildCycleTests {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val extendsPath    = "amf-client/shared/src/test/resources/resolution/extends/"
  val productionPath = "amf-client/shared/src/test/resources/production/"
  val resolutionPath = "amf-client/shared/src/test/resources/resolution/"
  val cyclePath      = "amf-client/shared/src/test/resources/upanddown/"
  val referencesPath    = "amf-client/shared/src/test/resources/references/"

  test("Simple extends resolution to Raml") {
    cycle("simple-merge.raml", "simple-merge.editing.jsonld", RamlYamlHint, Amf, extendsPath)
  }

  test("Types resolution to Raml") {
    cycle("data.raml", "data.editing.jsonld", RamlYamlHint, Amf, extendsPath)
  }

  test("Example1 resolution to Raml") {
    cycle("example1.yaml", "example1.resolved.yaml", OasYamlHint, Oas2Yaml, resolutionPath)
  }

  test("Include type resolution to Raml") {
    cycle("simple_example_type.raml", "simple_example_type.resolved.jsonld", RamlYamlHint, Amf, cyclePath)
  }

  test("Test data type fragment resolution to Amf") {
    cycle("data-type-fragment.reference.raml", "data-type-fragment.reference.resolved.jsonld", RamlYamlHint, Amf, referencesPath)
  }

  test("Test union arrays") {
    cycle("union_arrays.raml", "union_arrays.resolved.jsonld", RamlYamlHint, Amf, cyclePath)
  }

  /*
  test("Exchange experience API resolution to Amf") {
    cycle("api.v1.raml", "api.v1.resolved.jsonld", RamlYamlHint, Amf, productionPath + "exchange-experience-api-1.0.1-raml/")
  }

  ignore("Github API resolution to Raml") {
    cycle("api.raml", "api.jsonld", RamlYamlHint, Amf, productionPath + "github-api-1.0.0-raml/")
  }

  test("Google API resolution to Raml") {
    cycle("googleapis.compredictionv1.2swagger.raml", "googleapis.compredictionv1.2swagger.raml", RamlYamlHint, Amf, productionPath)
  }

  test("Financial API resolution to Raml") {
    cycle("infor-financial-api.raml", "infor-financial-api.jsonld", RamlYamlHint, Amf, productionPath + "financial-api/")
  }
  */

  override def transform(unit: BaseUnit, config: CycleConfig): BaseUnit = config.target match {
    case Raml08                => RAML08Plugin.resolve(unit, ResolutionPipeline.EDITING_PIPELINE)
    case Raml | Raml10         => RAML10Plugin.resolve(unit, ResolutionPipeline.EDITING_PIPELINE)
    case Oas3                  => OAS30Plugin.resolve(unit, ResolutionPipeline.EDITING_PIPELINE)
    case Oas | Oas2 | Oas2Yaml => OAS20Plugin.resolve(unit, ResolutionPipeline.EDITING_PIPELINE)
    case Amf                   => new AmfEditingPipeline().resolve(unit)
    case target                => throw new Exception(s"Cannot resolve $target")
    //    case _ => unit
  }

  override def render(unit: BaseUnit, config: CycleConfig): Future[String] = {
    new AMFRenderer(unit, config.target, config.target.defaultSyntax, RenderOptions().withSourceMaps.withRawSourceMaps.withCompactUris).renderToString
  }

  override val basePath: String = ""
}
