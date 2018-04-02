package amf.resolution

import amf.core.emitter.RenderOptions
import amf.core.model.document.BaseUnit
import amf.core.remote.{Amf, Raml, Raml08, RamlYamlHint}
import amf.facades.AMFRenderer

import scala.concurrent.Future

abstract class RamlResolutionTest extends ResolutionTest {
  override def render(unit: BaseUnit, config: CycleConfig): Future[String] =
    new AMFRenderer(unit, config.target, Raml.defaultSyntax, RenderOptions().withSourceMaps).renderToString
}

class ProductionResolutionTest extends RamlResolutionTest {
  override val basePath = "amf-client/shared/src/test/resources/production/"

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

  test("Recursive union raml to amf") {
    cycle("recursive-union.raml", "recursive-union.raml.jsonld", RamlYamlHint, Amf)
  }

  ignore("API Console test api") {
    cycle("api.raml", "api.resolved.jsonld", RamlYamlHint, Amf, basePath + "api-console/")
  }

  test("test resource type") {
    cycle("input.raml", "input.resolved.raml", RamlYamlHint, Raml,"amf-client/shared/src/test/resources/org/raml/api/v10/library-references-absolute/" )
  }

  test("test resource type non string scalar parameter example") {
    cycle("input.raml", "input.resolved.raml", RamlYamlHint, Raml,"amf-client/shared/src/test/resources/org/raml/parser/resource-types/non-string-scalar-parameter/" )
  }

  test("test problem inclusion parent test") {
    cycle("input.raml", "input.resolved.raml", RamlYamlHint, Raml,basePath + "include-parent/" )
  }
}

class Raml08ResolutionTest extends RamlResolutionTest {
  override val basePath: String = "amf-client/shared/src/test/resources/resolution/08/"

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
