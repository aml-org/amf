package amf.dumper

import amf.client.GenerationOptions
import amf.document.BaseUnit
import amf.domain.extensions.idCounter
import amf.emit.AMFUnitMaker
import amf.remote.Syntax.{Json, Syntax, Yaml}
import amf.remote._
import org.yaml.render.{JsonRender, YamlRender}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AMFDumper(unit: BaseUnit, vendor: Vendor, syntax: Syntax, options: GenerationOptions) {

  private val ast = AMFUnitMaker(unit, vendor, options)

  /** Print ast to string. */
  def dumpToString: String = dump()

  /** Print ast to file. */
  def dumpToFile(remote: Platform, path: String): Future[Unit] = remote.write(path, dump())

  private def dump(): String = {
    // reset data node counter
    idCounter.reset()

    vendor match {
      case Raml =>
        syntax match {
          case Yaml => YamlRender.render(ast)
          case _    => unsupported
        }
      case Oas | Amf | Payload =>
        syntax match {
          case Json => JsonRender.render(ast)
          case _    => unsupported
        }
      case Unknown => unsupported
    }
  }

  private def unsupported = {
    throw new RuntimeException(s"Unsupported '$syntax' syntax for '$vendor'")
  }
}

object AMFDumper {
  def apply(unit: BaseUnit, vendor: Vendor, syntax: Syntax, options: GenerationOptions): AMFDumper =
    new AMFDumper(unit, vendor, syntax, options)
}
