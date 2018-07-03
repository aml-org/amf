package amf

import java.io.{InputStream, PrintStream}
import java.util.Scanner

import amf.client.model.document.{BaseUnit, Document}
import amf.client.parse._
import amf.client.render._
import amf.convert.NativeOpsFromJvm
import amf.core.remote._
import scala.concurrent.ExecutionContext.Implicits.global
class Repl(val in: InputStream, val out: PrintStream) extends NativeOpsFromJvm {

  init()

  private def init(): Unit = {
    val scanner                = new Scanner(in)
    var unit: Option[Document] = None

    while (scanner.hasNextLine) {
      scanner.nextLine() match {
        case Exit()               => return
        case Parse((vendor, url)) => remote(vendor, url, unit = _)
        case Generate(syntax)     => unit.foreach(doc => generate(doc, syntax))
        case line                 => out.println(s"... $line")
      }
    }
  }

  private def generate(unit: BaseUnit, syntax: String): Unit = {
    val generator: Option[Renderer] = syntax match {
      case "raml"   => Some(new Raml10Renderer)
      case "raml08" => Some(new Raml08Renderer)
      case "oas"    => Some(new Oas20Renderer)
      case "amf"    => Some(new AmfGraphRenderer)
      case _ =>
        out.println(s"Unsupported generation for: $syntax")
        None
    }

    generator.foreach(g => {
      g.generateString(unit).asFuture.map(out.print)
    })
  }

  private def remote(vendor: Vendor, url: String, callback: (Option[Document]) => Unit): Unit = {
    parser(vendor)
      .parseFileAsync(url)
      .asFuture
      .map({
        case d: Document => callback(Some(d))
        case _           => callback(None)
      })
  }

  private object Parse {
    def unapply(line: String): Option[(Vendor, String)] = {
      line match {
        case s if s.startsWith(":raml ") => Some((Raml, s.stripPrefix(":raml ")))
        case s if s.startsWith(":oas ")  => Some((Oas, s.stripPrefix(":oas ")))
        case s if s.startsWith(":amf ")  => Some((Amf, s.stripPrefix(":amf ")))
        case _                           => None
      }
    }
  }

  private def parser(vendor: Vendor) = vendor match {
    case Raml10  => new Raml10Parser
    case Raml08  => new Raml08Parser
    case Raml    => new RamlParser
    case Oas     => new Oas20Parser
    case Amf     => new AmfGraphParser
    case Payload => throw new Exception("Cannot find a parser for Payload vendor")
    case _       => throw new Exception("Cannot find a parser for Unknown vendor")
  }

  private object Generate {
    def unapply(line: String): Option[String] = {
      line match {
        case s if s.startsWith(":generate ") => Some(s.stripPrefix(":generate "))
        case _                               => None
      }
    }
  }

  private object Exit {
    def unapply(line: String): Boolean = {
      line match {
        case ":exit" | ":quit" | ":q" => true
        case _                        => false
      }
    }
  }

}

object Repl {
  def apply(in: InputStream, out: PrintStream): Repl = new Repl(in, out)
}
