package amf.resolution

import amf.ProfileNames
import amf.client.GenerationOptions
import amf.document.BaseUnit
import amf.dumper.AMFDumper
import amf.io.BuildCycleTests
import amf.remote._

import scala.concurrent.Future

abstract class ResolutionTest extends BuildCycleTests {

  override def map(unit: BaseUnit, config: CycleConfig): BaseUnit = config.target match {
    case Raml   => unit.resolve(ProfileNames.RAML)
    case Oas    => unit.resolve(ProfileNames.OAS)
    case Amf    => unit.resolve(ProfileNames.AMF)
    case target => throw new Exception(s"Cannot resolve $target")
  }

  override def render(unit: BaseUnit, config: CycleConfig): Future[String] =
    new AMFDumper(unit, Amf, Amf.defaultSyntax, GenerationOptions().withSourceMaps).dumpToString
}
