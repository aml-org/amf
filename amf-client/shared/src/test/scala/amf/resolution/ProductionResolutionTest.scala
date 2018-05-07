package amf.resolution

import amf.core.emitter.RenderOptions
import amf.core.model.document.BaseUnit
import amf.core.remote._
import amf.facades.{AMFCompiler, AMFRenderer, Validation}
import amf.plugins.document.webapi.{OAS20Plugin, OAS30Plugin, RAML08Plugin, RAML10Plugin}

import scala.concurrent.Future

abstract class RamlResolutionTest extends ResolutionTest {
  override def render(unit: BaseUnit, config: CycleConfig): Future[String] = {
    new AMFRenderer(unit, config.target, Raml.defaultSyntax, RenderOptions().withSourceMaps).renderToString
  }
}

abstract class OasResolutionTest extends ResolutionTest {
  override def transform(unit: BaseUnit, config: CycleConfig): BaseUnit = {
    val res = config.target match {
      case Raml08        => RAML08Plugin.resolve(unit)
      case Raml | Raml10 => RAML10Plugin.resolve(unit)
      case Oas3          => OAS30Plugin.resolve(unit)
      case Oas | Oas2    => OAS20Plugin.resolve(unit)
      case Amf           => OAS20Plugin.resolve(unit)
      case target        => throw new Exception(s"Cannot resolve $target")
      //    case _ => unit
    }
    res
  }
}

class ProdcutionValidationTest extends RamlResolutionTest {
  override val basePath = "amf-client/shared/src/test/resources/production/"
  override def build(config: CycleConfig, given: Option[Validation]): Future[BaseUnit] = {
    val validation: Future[Validation] = given match {
      case Some(validation: Validation) => Future { validation }
      case None                         => Validation(platform).map(_.withEnabledValidation(true))
    }
    validation.flatMap { v =>
      AMFCompiler(s"file://${config.sourcePath}", platform, config.hint, v).build()
    }
  }

  test("Recursive union raml to amf") {
    cycle("recursive-union.raml", "recursive-union.raml.jsonld", RamlYamlHint, Amf)
  }

}

class ProductionResolutionTest extends RamlResolutionTest {
  override val basePath = "amf-client/shared/src/test/resources/production/"
  val completeCyclePath = "amf-client/shared/src/test/resources/upanddown/"
  val validationPath    = "amf-client/shared/src/test/resources/validations/"

  test("Resolves googleapis.compredictionv1.2swagger.raml") {
    cycle("googleapis.compredictionv1.2swagger.raml",
          "googleapis.compredictionv1.2swagger.raml.resolved.raml",
          RamlYamlHint,
          Raml)
  }

  test("Resolves channel4.com1.0.0swagger.raml") {
    cycle("channel4.com1.0.0swagger.raml", "channel4.com1.0.0swagger.resolved.raml", RamlYamlHint, Raml)
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

  test("httpbin example") {
    cycle("httpbin.raml", "httpbin.resolved.raml", RamlYamlHint, Raml, basePath + "httpbin/")
  }

  test("sales-order example") {
    cycle("sales-order-api.raml", "sales-order-api.resolved.raml", RamlYamlHint, Raml, basePath + "order-api/")
  }

  test("american-flights-api example") {
    cycle("american-flights-api.raml",
          "american-flights-api.resolved.raml",
          RamlYamlHint,
          Raml,
          basePath + "american-flights-api/")
  }

  test("version-manager example") {
    cycle("version_manager.raml", "version_manager.resolved.raml", RamlYamlHint, Raml, basePath + "version-manager/")
  }

  ignore("API Console test api") {
    cycle("api.raml", "api.resolved.jsonld", RamlYamlHint, Amf, basePath + "api-console/")
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

  test("test api_6109_ver_10147") {
    cycle("api.raml", "api.resolved.raml", RamlYamlHint, Raml, basePath + "api_6109_ver_10147/")
  }

  test("test definition_loops input") {
    cycle("crossfiles2.raml", "crossfiles2.resolved.raml", RamlYamlHint, Raml, basePath + "definitions-loops/")
  }

  test("test bad tabulation at end flow map of traits definitions") {
    cycle("healthcare-system-api.raml",
          "healthcare-system-api.resolved.raml",
          RamlYamlHint,
          Raml,
          basePath + "Healthcare-FHIR-System-API/")
  }

  test("test trait with quoted string example var") {
    cycle("trait-string-quoted-node.raml",
          "trait-string-quoted-node.resolved.raml",
          RamlYamlHint,
          Raml,
          completeCyclePath)
  }

  test("test nullpointer in resolution") {
    cycle("api.raml", "api.resolved.raml", RamlYamlHint, Raml, validationPath + "retail-api-6.0.0-fat-raml/")
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
      cycle("wrong-key.raml", "wrong-key.raml", RamlYamlHint, Raml08)
    }.map { ex =>
      assert(ex.getMessage.contains("Message: Property errorKey not supported in a raml 0.8 webApi node"))
    }
  }

  test("Test empty trait in operations") {
    cycle("empty-is-operation-endpoint.raml", "empty-is-operation-endpoint.raml.raml", RamlYamlHint, Raml08)
  }
}
