package amf.resolution

import amf.core.client.GenerationOptions
import amf.core.model.document.BaseUnit
import amf.core.remote.{Raml, RamlYamlHint}
import amf.facades.AMFDumper

abstract class RamlResolutionTest extends ResolutionTest {
  override def render(unit: BaseUnit, config: CycleConfig): String =
    new AMFDumper(unit, Raml, Raml.defaultSyntax, GenerationOptions().withSourceMaps).dumpToString
}

class ProductionResolutionTest extends RamlResolutionTest  {
  override val basePath = "amf-client/shared/src/test/resources/production/"

  test("Resolves googleapis.compredictionv1.2swagger.raml") {
    cycle("googleapis.compredictionv1.2swagger.raml", "googleapis.compredictionv1.2swagger.raml.resolved.raml", RamlYamlHint, Raml)
  }

  test("Resolves channel4.com1.0.0swagger.raml") {
    cycle("channel4.com1.0.0swagger.raml", "channel4.com1.0.0swagger.resolved.raml", RamlYamlHint, Raml)
  }

}
