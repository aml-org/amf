package amf.resolution

import amf.client.render.RenderOptions
import amf.core.model.document.BaseUnit
import amf.core.remote.{Amf, Raml, Raml08, RamlYamlHint}
import amf.facades.AMFDumper

abstract class RamlResolutionTest extends ResolutionTest {
  override def render(unit: BaseUnit, config: CycleConfig): String =
    new AMFDumper(unit, config.target, Raml.defaultSyntax, RenderOptions().withSourceMaps).dumpToString
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
