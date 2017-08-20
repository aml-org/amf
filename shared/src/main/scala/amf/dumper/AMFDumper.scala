package amf.dumper

import amf.document.BaseUnit
import amf.emit.AMFUnitMaker
import amf.generator.{JsonGenerator, YamlGenerator}
import amf.remote._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

class AMFDumper(unit: BaseUnit, vendor: Vendor) {

  val ast = AMFUnitMaker(unit, vendor)

  def dumpToStream: Future[String] = Future {
    dump()
  }

  private def dump(): String = {
    vendor match {
      case Raml      => new YamlGenerator().generate(ast).toString
      case Oas | Amf => new JsonGenerator().generate(ast).toString
    }
  }

  def dumpToFile(remote: Platform, path: String): Future[String] = remote.write(path, dump()).map(p => p)
}

object AMFDumper {
  def apply(unit: BaseUnit, vendor: Vendor): AMFDumper = new AMFDumper(unit, vendor)
}
