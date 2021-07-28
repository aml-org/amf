package amf.cli.client

import amf.apicontract.client.platform.{APIConfiguration, AsyncAPIConfiguration, WebAPIConfiguration}
import amf.cli.internal.convert.NativeOpsFromJvm
import amf.core.client.platform.AMFResult
import amf.core.client.platform.model.document.{BaseUnit, Document}
import amf.core.internal.remote._

import java.io.{InputStream, PrintStream}
import java.util.Scanner
import scala.concurrent.ExecutionContext.Implicits.global

class Repl(val in: InputStream, val out: PrintStream) extends NativeOpsFromJvm {

  init()
  private val config = APIConfiguration.API()
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

  private def generate(unit: BaseUnit, mediaType: String): Unit = {
    val client = config.baseUnitClient()
    out.print(client.render(unit, mediaType))
  }

  private def remote(vendor: SpecId, url: String, callback: (Option[Document]) => Unit): Unit = {
    val client = config.baseUnitClient()

    client
      .parse(url, vendor.mediaType)
      .asFuture
      .map({
        case r: AMFResult if r.baseUnit.isInstanceOf[Document] => callback(Some(r.baseUnit.asInstanceOf[Document]))
        case _                                                 => callback(None)
      })
  }

  private object Parse {
    def unapply(line: String): Option[(SpecId, String)] = {
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
