package amf.dumper

import amf.document.BaseUnit
import amf.emit.AMFUnitMaker
import amf.generator.{JsonGenerator, YamlGenerator}
import amf.remote._

import scala.concurrent.Future

class AMFDumper(unit: BaseUnit, vendor: Vendor) {

  val ast = AMFUnitMaker(unit, vendor)

  def dump: String = vendor match {
    case Raml      => new YamlGenerator().generate(ast).toString
    case Oas | Amf => new JsonGenerator().generate(ast).toString
  }

  def dumpToFile(remote: Platform, path: String): Future[Unit] = remote.write(path, dump)
}
