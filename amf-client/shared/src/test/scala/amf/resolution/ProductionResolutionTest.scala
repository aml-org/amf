package amf.resolution

import amf.client.parse.DefaultParserErrorHandler
import amf.core.emitter.RenderOptions
import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.metamodel.document.DocumentModel
import amf.core.model.document.{BaseUnit, Document}
import amf.core.parser.errorhandler.{ParserErrorHandler, UnhandledParserErrorHandler}
import amf.core.remote._
import amf.core.resolution.stages.ReferenceResolutionStage
import amf.emit.AMFRenderer
import amf.facades.{AMFCompiler, Validation}
import amf.plugins.document.webapi.resolution.pipelines.AmfEditingPipeline
import amf.plugins.document.webapi.{Oas20Plugin, Oas30Plugin, Raml08Plugin, Raml10Plugin}
import org.scalatest.Assertion

import scala.concurrent.Future

abstract class RamlResolutionTest extends ResolutionTest {
  override def render(unit: BaseUnit, config: CycleConfig, useAmfJsonldSerialization: Boolean): Future[String] = {
    new AMFRenderer(unit, config.target, RenderOptions().withSourceMaps.withPrettyPrint, config.syntax).renderToString
  }
}

abstract class OasResolutionTest extends ResolutionTest {
  override def transform(unit: BaseUnit, config: CycleConfig): BaseUnit = {
    val res = config.target match {
      case Raml08        => Raml08Plugin.resolve(unit, UnhandledErrorHandler)
      case Raml | Raml10 => Raml10Plugin.resolve(unit, UnhandledErrorHandler)
      case Oas30         => Oas30Plugin.resolve(unit, UnhandledErrorHandler)
      case Oas | Oas20   => Oas20Plugin.resolve(unit, UnhandledErrorHandler)
      case Amf           => Oas20Plugin.resolve(unit, UnhandledErrorHandler)
      case target        => throw new Exception(s"Cannot resolve $target")
      //    case _ => unit
    }
    res
  }
}

class ProductionValidationTest extends RamlResolutionTest {
  override val basePath = "amf-client/shared/src/test/resources/production/"
  override def build(config: CycleConfig,
                     eh: Option[ParserErrorHandler],
                     useAmfJsonldSerialization: Boolean): Future[BaseUnit] = {
    Validation(platform).flatMap { v =>
      AMFCompiler(s"file://${config.sourcePath}",
                  platform,
                  config.hint,
                  eh = eh.getOrElse(DefaultParserErrorHandler.withRun())).build()
    }
  }

  test("Recursive union raml to amf") {
    cycle("recursive-union.raml", "recursive-union.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Recursive union raml to raml") {
    cycle("recursive-union.raml", "recursive-union.raml.raml", RamlYamlHint, Raml)
  }

  test("Patch method raml to raml") {
    cycle("api.raml", "api.raml.raml", RamlYamlHint, Raml, directory = basePath + "patch-method/")
  }

}

class ProductionResolutionTest extends RamlResolutionTest {
  override val basePath = "amf-client/shared/src/test/resources/production/"
  val completeCyclePath = "amf-client/shared/src/test/resources/upanddown/"
  val validationPath    = "amf-client/shared/src/test/resources/validations/"
  val productionRaml10  = "amf-client/shared/src/test/resources/production/raml10/"
  val productionRaml08  = "amf-client/shared/src/test/resources/production/raml08/"

  test("Test declared type with facet added") {
    cycle("add-facet.raml", "add-facet.raml.jsonld", RamlYamlHint, Amf, basePath + "inherits-resolution-declares/")
  }

  test("Test inline type from includes") {
    cycle("test-ramlfragment.raml",
          "test-ramlfragment.raml.jsonld",
          RamlYamlHint,
          Amf,
          basePath + "inherits-resolution-declares/")
  }

  test("Resolves googleapis.compredictionv1.2swagger.raml") {
    cycle("googleapis.compredictionv1.2swagger.raml",
          "googleapis.compredictionv1.2swagger.raml.resolved.raml",
          RamlYamlHint,
          Raml)
  }

  test("Resolves googleapis.compredictionv1.2swagger.raml to jsonld") {
    cycle("googleapis.compredictionv1.2swagger.raml",
          "googleapis.compredictionv1.2swagger.raml.resolved.jsonld",
          RamlYamlHint,
          Amf)
  }

  test("azure_blob_service raml to jsonld") {
    cycle("microsoft_azure_blob_service.raml", "microsoft_azure_blob_service.raml.resolved.jsonld", RamlYamlHint, Amf)
  }

  test("test definition_loops input") {
    cycle("api.raml",
          "crossfiles2.resolved.raml",
          RamlYamlHint,
          Raml,
          productionRaml08 + "definitions-loops-crossfiles2/")
  }

  test("Types with unions raml to AMF") {
    cycle("unions-example.raml", "unions-example.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Examples in header of type union") {
    cycle("example-in-union.raml", "example-in-union.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Complex types raml to raml") {
    cycle("complex_types.raml", "complex_types.resolved.raml", RamlYamlHint, Raml)
  }

  test("sales-order example") {
    cycle("sales-order-api.raml", "sales-order-api.resolved.raml", RamlYamlHint, Raml, basePath + "order-api/")
  }

  test("american-flights-api example") {
    cycle("api.raml",
          "american-flights-api.resolved.raml",
          RamlYamlHint,
          Raml,
          productionRaml10 + "american-flights-api/")
  }

  ignore("API Console test api") {
    cycle("api.raml", "api.resolved.jsonld", RamlYamlHint, Amf, basePath + "api-console/")
  }

  test("Test trait resolution null pointer exception test") {
    cycle("e-bo.raml", "e-bo.resolved.raml", RamlYamlHint, Raml, basePath + "reference-api/")
  }

  test("Test lib trait resolution with type defined in lib") {
    cycle("api.raml", "api.resolved.raml", RamlYamlHint, Raml, basePath + "lib-trait-type-resolution/")
  }

  test("test resource type") {
    cycle("input.raml",
          "input.resolved.raml",
          RamlYamlHint,
          Raml,
          "amf-client/shared/src/test/resources/org/raml/api/v10/library-references-absolute/")
  }

  test("test resource type non string scalar parameter example") {
    cycle(
      "input.raml",
      "input.resolved.raml",
      RamlYamlHint,
      Raml,
      "amf-client/shared/src/test/resources/org/raml/parser/resource-types/non-string-scalar-parameter/"
    )
  }

  test("test problem inclusion parent test") {
    cycle("input.raml", "input.resolved.raml", RamlYamlHint, Raml, basePath + "include-parent/")
  }

  test("test overlay documentation") {
    cycle("overlay.raml", "api.resolved.raml", RamlYamlHint, Raml, basePath + "overlay-documentation/")
  }

  // TODO: diff of final result is too slow
  ignore("test api_6109_ver_10147") {
    cycle("api.raml", "api.resolved.raml", RamlYamlHint, Raml, basePath + "api_6109_ver_10147/")
  }

  test("test bad tabulation at end flow map of traits definitions") {
    cycle("healthcare.raml", "healthcare.resolved.raml", RamlYamlHint, Raml, basePath + "healthcare/")
  }

  test("test trait with quoted string example var") {
    cycle("trait-string-quoted-node.raml",
          "trait-string-quoted-node.resolved.raml",
          RamlYamlHint,
          Raml,
          completeCyclePath)
  }

  test("test nullpointer in resolution") {
    cycle("api.raml", "api.resolved.raml", RamlYamlHint, Raml, validationPath + "retail-api/")
  }

  test("Test resolve inherited array without items") {
    cycle("inherits-array-without-items.raml",
          "inherits-array-without-items.resolved.raml",
          RamlYamlHint,
          Raml,
          basePath + "types/")
  }

  test("Test resolve resource type with '$' char in variable value") {
    cycle("invalid-regexp-char-in-variable.raml",
          "invalid-regexp-char-in-variable.resolved.raml",
          RamlYamlHint,
          Raml,
          basePath)
  }

  test("Test type resolution with property override") {
    cycle("property-override.raml", "property-override.resolved.raml", RamlYamlHint, Raml, basePath + "types/")
  }

  test("Test endpoints are not removed") {
    val source     = "api.raml"
    val golden     = ""
    val hint       = RamlYamlHint
    val target     = Amf
    val directory  = productionRaml10 + "demo-api/"
    val syntax     = None
    val validation = None

    val config                    = CycleConfig(source, golden, hint, target, directory, syntax, None)
    val useAmfJsonldSerialization = true

    for {
      simpleModel <- build(config, validation, useAmfJsonldSerialization).map(AmfEditingPipeline.unhandled.resolve(_))
      a           <- render(simpleModel, config, useAmfJsonldSerialization)
      doubleModel <- build(config, validation, useAmfJsonldSerialization).map(AmfEditingPipeline.unhandled.resolve(_))
      _           <- render(doubleModel, config, useAmfJsonldSerialization)
      b           <- render(doubleModel, config, useAmfJsonldSerialization)
    } yield {
      val simpleDeclares = simpleModel.asInstanceOf[Document].declares
      val doubleDeclares = doubleModel.asInstanceOf[Document].declares
      writeTemporaryFile("demo-api1.jsonld")(a)
      writeTemporaryFile("demo-api2.jsonld")(b)
      assert(simpleDeclares.length == doubleDeclares.length)
    }
  }

  test("Test example inheritance in type declaration with simple inheritance") {
    cycle("api.raml", "api.raml.resolved", RamlYamlHint, Raml, basePath + "simple-inheritance-example/")
  }

  test("Test example inheritance in type declaration with simple chained inheritance") {
    cycle("api.raml", "api.raml.resolved", RamlYamlHint, Raml, basePath + "simple-inheritance-chained-example/")
  }

  test("Test example inheritance in type declaration with link") {
    cycle("api.raml", "api.raml.resolved", RamlYamlHint, Raml, basePath + "simple-inheritance-link-example/")
  }

  test("Test union type anyOf name values") {
    cycle("api.raml", "api.raml.resolved", RamlYamlHint, Amf, basePath + "union-type/")
  }

  test("Test complex recursions in type inheritance 1") {
    cycle("healthcare_reduced_v1.raml", "healthcare_reduced_v1.raml.resolved", RamlYamlHint, Amf, validationPath)
  }

  test("Test complex recursions in type inheritance 2") {
    cycle("healthcare_reduced_v2.raml", "healthcare_reduced_v2.raml.resolved", RamlYamlHint, Amf, validationPath)
  }

  test("Test resource type parameters ids") {
    cycle("rt-parameters.raml", "rt-parameters.raml.resolved", RamlYamlHint, Amf, validationPath)
  }

  test("Test nil type with additional facets") {
    cycle("nil-type.raml", "nil-type.raml.resolved", RamlYamlHint, Amf, validationPath)
  }
}

class OASProductionResolutionTest extends OasResolutionTest {
  override val basePath = "amf-client/shared/src/test/resources/production/"
  val completeCyclePath = "amf-client/shared/src/test/resources/upanddown/"

  test("OAS Response parameters resolution") {
    cycle("oas_response_declaration.yaml",
          "oas_response_declaration.resolved.jsonld",
          OasYamlHint,
          Amf,
          completeCyclePath)
  }

  test("OAS with foward references in definitions") {
    cycle("oas_foward_definitions.json", "oas_foward_definitions.resolved.jsonld", OasJsonHint, Amf, completeCyclePath)
  }

  test("OAS with external fragment reference in upper folder") {
    cycle("master/master.json", "api.resolved.jsonld", OasJsonHint, Amf, completeCyclePath + "oas-fragment-ref/")
  }

  test("OAS complex example") {
    cycle("spec/swagger.json", "api.resolved.jsonld", OasJsonHint, Amf, basePath + "oas-complex-example/")
  }

  test("OAS examples test") {
    cycle("oas-example.json", "oas-example.json.jsonld", OasJsonHint, Amf)
  }

  test("OAS multiple examples test") {
    cycle("oas-multiple-example.json", "oas-multiple-example.json.jsonld", OasJsonHint, Amf)
  }

  test("OAS XML payload test") {
    cycle("oas20/xml-payload.json", "oas20/xml-payload.json.jsonld", OasYamlHint, Amf)
  }

  test("Summary and description from path applied to operations") {
    cycle(
      "description-applied-to-operations.json",
      "description-applied-to-operations-resolution.jsonld",
      OasJsonHint,
      Oas30,
      completeCyclePath + "oas3/summary-description-in-path/"
    )
  }
}

class Raml08ResolutionTest extends RamlResolutionTest {
  override val basePath: String = "amf-client/shared/src/test/resources/resolution/08/"
  val productionPath: String    = "amf-client/shared/src/test/resources/production/"

  test("Resolve WebForm 08 Types test") {
    cycle("mincount-webform-types.raml", "mincount-webform-types.resolved.raml", RamlYamlHint, Raml08)
  }

  test("Resolve Min and Max in header 08 test") {
    cycle("min-max-in-header.raml", "min-max-in-header.resolved.raml", RamlYamlHint, Raml08)
  }

  test("Test failing with exception") {
    recoverToExceptionIf[Exception] {
      cycle("wrong-key.raml", "wrong-key.raml", RamlYamlHint, Raml08, eh = Some(UnhandledParserErrorHandler))
    }.map { ex =>
      assert(ex.getMessage.contains(s"Message: Property 'errorKey' not supported in a ${Raml08.name} webApi node"))
    }
  }

  test("Test empty trait in operations") {
    cycle("empty-is-operation-endpoint.raml", "empty-is-operation-endpoint.raml.raml", RamlYamlHint, Raml08)
  }

  test("Test included schema") {
    cycle("api.raml", "api.raml.raml", RamlYamlHint, Raml08, basePath + "included-schema/")
  }

  test("Test included schema and example") {
    cycle("api.raml", "api.jsonld", RamlYamlHint, Amf, basePath + "included-schema-and-example/")
  }

  test("Test json_schemasa refs") {
    cycle("json_schemas.raml", "json_schemas.resolved.raml", RamlYamlHint, Raml08)
  }

}

/**
  * This unit tests run as one, we need several steps to check that the dumped json ld, after resolve types, it's correct.
  * That means, that not only the graph can be parsed, but also it's similar to the resolved model dumped as raml.
  * In order to check that, first dump a raml to jsonld to clean the annotations. Then, we parse that jsonld, resolve the model and dump it to raml.
  * We do that, to get a raml api generated from a resolved model without annotations
  * After check that, we parse the resolved jsonld model, and generates the raml, to check that the final raml it's equivalent to the raml resolved and dumped.
  * */
class ProductionServiceTest extends RamlResolutionTest {

  override def build(config: CycleConfig,
                     eh: Option[ParserErrorHandler],
                     useAmfJsonldSerialization: Boolean): Future[BaseUnit] = {
    Validation(platform).flatMap { v =>
      AMFCompiler(s"file://${config.sourcePath}", platform, config.hint, eh = UnhandledParserErrorHandler).build()
    }
  }
  private def dummyFunc: (BaseUnit, CycleConfig) => BaseUnit = (u: BaseUnit, _: CycleConfig) => u

  override val basePath = "amf-client/shared/src/test/resources/production/resolution-dumpjsonld/"

  /* Generate the jsonld from a resolved raml */
  test("Test step1: resolve and emit jsonld") {
    run("api.raml", "api.resolved.raml.jsonld", RamlYamlHint, Amf, transform)
  }

  /* Generate the api resolved directly, without serialize the jsonld */
  test("Test step2: resolve and emit raml") {
    run("api.raml", "api.resolved.raml", RamlYamlHint, Raml, transform)
  }

  /* Generate the jsonld without resolve (to clean the annotations) */
  test("Test step3: emit jsonld without resolve") {
    run("api.raml", "api.raml.jsonld", RamlYamlHint, Amf, dummyFunc)
  }

  /* Generate the resolved raml after read the jsonld(without annotations) */
  test("Test step4: emit jsonld with resolve") {
    run("api.raml.jsonld", "api.raml.jsonld.resolved.raml", AmfJsonHint, Raml, transform)
  }

  /* Now we really test the case, parse the json ld and compare to a similar jsonld (this should have the declarations) */
  test("Test step5: parse resolved api and dump raml") {
    run("api.resolved.raml.jsonld", "api.resolved.jsonld.raml", AmfJsonHint, Raml, dummyFunc)
  }

  /* Generate the raml from a json ld without resolve */
  test("Test step6: parse resolved api and dump raml") {
    run("api.raml.jsonld", "api.jsonld.raml", AmfJsonHint, Raml, dummyFunc)
  }

  /* Generate the raml from a jsonld resolved raml */
  test("Test step7: emit resolved jsonld and check against normal raml") {
    run("api.resolved.raml.jsonld", "api.jsonld.raml", AmfJsonHint, Raml, dummyFunc)
  }

  /* Generate the raml api from a resolved raml to jsonld cleaning the declarations and refs stage */
  test("Test step8: emit resolved jsonld and check against normal raml") {
    run(
      "api.resolved.raml.jsonld",
      "api.raml.jsonld.resolved.raml",
      AmfJsonHint,
      Raml,
      (u: BaseUnit, _: CycleConfig) => {
        val resolved = new ReferenceResolutionStage(false)(UnhandledErrorHandler).resolve(u)
        resolved.fields.removeField(DocumentModel.Declares)
        resolved
      }
    )
  }

  def run(source: String,
          golden: String,
          hint: Hint,
          target: Vendor,
          tFn: (BaseUnit, CycleConfig) => BaseUnit): Future[Assertion] = {

    val config = CycleConfig(source, golden, hint, target, basePath, None, None)

    build(config, None, useAmfJsonldSerialization = true)
      .map(tFn(_, config))
      .flatMap(render(_, config, useAmfJsonldSerialization = true))
      .flatMap(writeTemporaryFile(golden))
      .flatMap(assertDifferences(_, config.goldenPath))
  }

//  private def cycle(source: String, directory: String): Future[Assertion] = {
//
//    val config = CycleConfig(source, source+".jsonld", RamlYamlHint, Amf, directory)
//
//    for{
//      v <- Validation(platform).map(_.withEnabledValidation(true))
//      model <- build(config, Some(v))
//      tr <- Future.successful(transform(model,config))
//      jsonLd <- AMFRenderer(tr,Amf,Json,RenderOptions()).renderToString
//      resolvedRaml <- AMFRenderer(tr,Raml,Yaml,RenderOptions()).renderToString
//      parsedRaml <- AMFCompiler("", TrunkPlatform(jsonLd),AmfJsonHint,v).build()
//      resolvedParsedRaml <- Future.successful({
//        val unit = new ValidationResolutionPipeline(RAMLProfile, parsedRaml).resolve()
//        unit.fields.removeField(DocumentModel.Declares)
//        unit
//      })
//      renderedRaml <-  AMFRenderer(resolvedParsedRaml,Raml,Yaml,RenderOptions()).renderToString
//      resolvedFile <- writeTemporaryFile("resolved.raml")(resolvedRaml)
//      renderedFile <- writeTemporaryFile("rendered.raml")(renderedRaml)
//      r <- Tests.checkDiff(resolvedFile,renderedFile)
//    } yield {
//      r
//    }
//  }
}
