package amf.dumper

import amf.emit.AMFUnitMaker
import amf.generator.{JsonGenerator, YamlGenerator}
import amf.domain.WebApi
import amf.parser.{AMFUnit, Document, Fragment, Module}
import amf.remote._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AMFDumper(webApi: WebApi, vendor: Vendor) {
  val unitF = Future { AMFUnitMaker(webApi, vendor) }

  def dump(): Future[String] = {
    unitF.map(u =>
      u.`type` match {
        case Document =>
          dumpDocument(u)
        case Fragment => ???
        case Module   => ???
    })

  }

  def dumpToFile(remote: Platform, path: String): Unit = {
    dump().map(d => {
      remote.write(path, d)
    })
  }

  private def dumpDocument(unit: AMFUnit): String = { //TODO case for syntax instead of vendor?
    unit.vendor match {
      case Raml =>
        val yamlWritter = new YamlGenerator().generate(unit.root)
        yamlWritter.toString
      case Oas | Amf =>
        val oasWritter = new JsonGenerator().generate(unit.root)
        oasWritter.toString
      case _ => ???

    }
  }
}
