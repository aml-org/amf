package amf.resolution

import amf.core.client.GenerationOptions
import amf.core.model.document.BaseUnit
import amf.core.remote.{Amf, Oas, Raml}
import amf.facades.AMFDumper
import amf.io.BuildCycleTests
import amf.plugins.document.webapi.resolution.pipelines.AmfResolutionPipeline
import amf.plugins.document.webapi.{OAS20Plugin, RAML10Plugin}

abstract class ResolutionTest extends BuildCycleTests {

  override def transform(unit: BaseUnit, config: CycleConfig): BaseUnit = config.target match {
    case Raml   => RAML10Plugin.resolve(unit)
    case Oas    => OAS20Plugin.resolve(unit)
    case Amf    => new AmfResolutionPipeline().resolve(unit)
    case target => throw new Exception(s"Cannot resolve $target")
  }

  override def render(unit: BaseUnit, config: CycleConfig): String =
    new AMFDumper(unit, Amf, Amf.defaultSyntax, GenerationOptions().withSourceMaps).dumpToString
}
