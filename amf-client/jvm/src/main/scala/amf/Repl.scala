package amf

import amf.client.environment.{AsyncAPIConfiguration, WebAPIConfiguration}
import amf.client.model.document.{BaseUnit, Document}
import amf.client.remod.AMFResult
import amf.client.render._
import amf.convert.NativeOpsFromJvm
import amf.core.remote._

import java.io.{InputStream, PrintStream}
import java.util.Scanner
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
      case Raml10.name     => Some(new Raml10Renderer)
      case Raml08.name     => Some(new Raml08Renderer)
      case Oas30.name      => Some(new Oas30Renderer)
      case Oas20.name      => Some(new Oas20Renderer)
      case AsyncApi20.name => Some(new Async20Renderer)
      case Amf.name        => Some(new AmfGraphRenderer)
      case _ =>
        out.println(s"Unsupported generation for: $syntax")
        None
    }

    generator.foreach(g => {
      g.generateString(unit).asFuture.map(out.print)
    })
  }

  private def remote(vendor: Vendor, url: String, callback: (Option[Document]) => Unit): Unit = {
    val client = WebAPIConfiguration.WebAPI().merge(AsyncAPIConfiguration.Async20()).createClient()

    client
      .parse(url, vendor.mediaType)
      .map({
        case AMFResult(d: Document, _) => callback(Some(d))
        case _                         => callback(None)
      })
  }

  private object Parse {
    def unapply(line: String): Option[(Vendor, String)] = {
      line match {
        case s if s.startsWith(":application/raml10 ") => Some((Raml10, s.stripPrefix(":application/raml10 ")))
        case s if s.startsWith(":application/raml08 ") => Some((Raml08, s.stripPrefix(":application/raml08 ")))
        case s if s.startsWith(":application/oas20 ")  => Some((Oas20, s.stripPrefix(":application/oas20 ")))
        case s if s.startsWith(":application/oas30 ")  => Some((Oas30, s.stripPrefix(":application/oas30 ")))
        case s if s.startsWith(":application/async ")  => Some((AsyncApi, s.stripPrefix(":application/async ")))
        case s if s.startsWith(":application/amf ")    => Some((Amf, s.stripPrefix(":application/amf ")))
        case _                                         => None
      }
    }
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
