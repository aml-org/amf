package amf.resolution

import amf.core.emitter.RenderOptions
import amf.core.model.document.BaseUnit
import amf.core.remote.Syntax.Yaml
import amf.core.remote._
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.emit.AMFRenderer
import amf.facades.Validation
import amf.io.{BuildCycleTests, FunSuiteCycleTests}
import amf.plugins.document.webapi.resolution.pipelines.AmfEditingPipeline
import amf.plugins.document.webapi.{Oas20Plugin, Oas30Plugin, Raml08Plugin, Raml10Plugin}

import scala.concurrent.{ExecutionContext, Future}

class EditingResolutionTest extends FunSuiteCycleTests {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val extendsPath     = "amf-client/shared/src/test/resources/resolution/extends/"
  val productionPath  = "amf-client/shared/src/test/resources/production/"
  val resolutionPath  = "amf-client/shared/src/test/resources/resolution/"
  val cyclePath       = "amf-client/shared/src/test/resources/upanddown/"
  val referencesPath  = "amf-client/shared/src/test/resources/references/"
  val validationsPath = "amf-client/shared/src/test/resources/validations/"

  test("Simple extends resolution to Raml") {
    cycle("simple-merge.raml", "simple-merge.editing.jsonld", RamlYamlHint, Amf, extendsPath)
  }

  test("Types resolution to Raml") {
    cycle("data.raml", "data.editing.jsonld", RamlYamlHint, Amf, extendsPath)
  }

  test("Example1 resolution to Raml") {
    cycle("example1.yaml", "example1.resolved.yaml", OasYamlHint, Oas20, resolutionPath, syntax = Some(Yaml))
  }

  test("Include type resolution to Raml") {
    cycle("simple_example_type.raml", "simple_example_type.resolved.jsonld", RamlYamlHint, Amf, cyclePath)
  }

  test("Test data type fragment resolution to Amf") {
    cycle("data-type-fragment.reference.raml",
          "data-type-fragment.reference.resolved.jsonld",
          RamlYamlHint,
          Amf,
          referencesPath)
  }

  test("Test union arrays") {
    cycle("union_arrays.raml", "union_arrays.resolved.jsonld", RamlYamlHint, Amf, cyclePath)
  }

  test("Exchange issueNil API resolution to Amf") {
    cycle("api.raml", "api.resolved.jsonld", RamlYamlHint, Amf, validationsPath + "examples/inline-named-examples/")
  }

  test("Location in annotation of Trait declared in lib") {
    cycle("api.raml", "api.resolved.jsonld", RamlYamlHint, Amf, productionPath + "lib-trait-location/")
  }

  test("Test merge examples in local against declared type") {
    cycle("merge-examples.raml", "merge-examples.resolved.raml", RamlYamlHint, Raml, resolutionPath + "examples/")
  }

  test("Test extension merging") {
    cycle("input.raml",
          "input.resolved.jsonld",
          RamlYamlHint,
          Amf,
          "amf-client/shared/src/test/resources/resolution/extension/traits/")
  }

  test("Unresolved shape") {
    Validation(platform)
      .map(_.withEnabledValidation(true))
      .flatMap(
        v =>
          cycle("unresolved-shape.raml",
                "unresolved-shape.raml.jsonld",
                RamlYamlHint,
                Amf,
                resolutionPath,
                validation = Some(v)))
  }

  test("Test url shortener with external references") {
    cycle("api.raml",
          "api.resolved.jsonld",
          RamlYamlHint,
          Amf,
          resolutionPath + "externalfragment/test-links-with-references/")
  }

  test("Test tracked examples annotations parent shortened") {
    cycle("payloads-examples-resolution.raml",
          "payloads-examples-resolution.resolved.jsonld",
          RamlYamlHint,
          Amf,
          resolutionPath)
  }

  test("Test recursive annotations of extension provenance") {
    cycle("api.raml", "api.resolved.jsonld", RamlYamlHint, Amf, resolutionPath + "recursive-extension-provenance/")
  }

  test("Test url shortener at example (dynamic)") {
    cycle("examples-shortener.raml", "examples-shortener.resolved.jsonld", RamlYamlHint, Amf, resolutionPath)
  }

  test("Test double declared included type") {
    cycle("api.raml", "api.resolved.jsonld", RamlYamlHint, Amf, resolutionPath + "/double-declare-type/")
  }

  test("Test declared type from library") {
    cycle("api.raml", "api.resolved.jsonld", RamlYamlHint, Amf, resolutionPath + "/declared-from-library/")
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

  override def transform(unit: BaseUnit, config: CycleConfig): BaseUnit =
    config.target match {
      case Raml08        => Raml08Plugin.resolve(unit, ResolutionPipeline.EDITING_PIPELINE)
      case Raml | Raml10 => Raml10Plugin.resolve(unit, ResolutionPipeline.EDITING_PIPELINE)
      case Oas30         => Oas30Plugin.resolve(unit, ResolutionPipeline.EDITING_PIPELINE)
      case Oas | Oas20   => Oas20Plugin.resolve(unit, ResolutionPipeline.EDITING_PIPELINE)
      case Amf           => new AmfEditingPipeline(unit).resolve()
      case target        => throw new Exception(s"Cannot resolve $target")
    }

  override def render(unit: BaseUnit, config: CycleConfig, useAmfJsonldSerialization: Boolean): Future[String] = {
    new AMFRenderer(unit,
                    config.target,
                    RenderOptions().withSourceMaps.withRawSourceMaps.withCompactUris,
                    config.syntax).renderToString
  }

  override val basePath: String = ""
}
