package amf.resolution

import amf.ProfileNames
import amf.core.client.GenerationOptions
import amf.facades.AMFDumper
import amf.framework.model.document.BaseUnit
import amf.framework.remote.{Amf, Oas, Raml}
import amf.io.BuildCycleTests
import amf.remote._

import scala.concurrent.Future

abstract class ResolutionTest extends BuildCycleTests {

  override def transform(unit: BaseUnit, config: CycleConfig): BaseUnit = config.target match {
    case Raml   => unit.resolve(ProfileNames.RAML)
    case Oas    => unit.resolve(ProfileNames.OAS)
    case Amf    => unit.resolve(ProfileNames.AMF)
    case target => throw new Exception(s"Cannot resolve $target")
  }

  override def render(unit: BaseUnit, config: CycleConfig): String =
    new AMFDumper(unit, Amf, Amf.defaultSyntax, GenerationOptions().withSourceMaps).dumpToString
}
