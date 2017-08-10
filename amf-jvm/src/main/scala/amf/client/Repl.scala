package amf.client

import java.io.{InputStream, PrintStream}
import java.util.Scanner

import amf.dumper.AMFDumper
import amf.emit.AMFUnitMaker
import amf.model.{BaseUnit, Document}
import amf.parser.ASTNodePrinter
import amf.remote._

class Repl(val in: InputStream, val out: PrintStream) {

  init()

  private def init(): Unit = {
    val scanner                = new Scanner(in)
    var unit: Option[Document] = None

    while (scanner.hasNextLine) {
      scanner.nextLine() match {
        case Exit()           => return
        case Json(url)        => remote(url, OasJsonHint, unit = _)
        case Yaml(url)        => remote(url, RamlYamlHint, unit = _)
        case Ast(_)           => unit.foreach(doc => out.println(ASTNodePrinter.print(AMFUnitMaker(doc.document, Raml))))
        case Generate(syntax) => unit.foreach(doc => generate(doc, syntax))
        case line             => out.println(s"... $line")
      }
    }
  }

  private def generate(unit: BaseUnit, syntax: String): Unit = {
    syntax match {
      case "json"   => out.println(new AMFDumper(unit.unit, Oas).dump)
      case "yaml"   => out.println(new AMFDumper(unit.unit, Raml).dump)
      case "jsonld" => out.println(new AMFDumper(unit.unit, Amf).dump)
      case _        => out.println(s"Unsupported generation for: $syntax")
    }
  }

  private def remote(url: String, hint: Hint, callback: (Option[Document]) => Unit): Unit = {
    new JvmClient().generateFromFile(
      url,
      hint,
      new Handler[BaseUnit] {
        override def success(unit: BaseUnit): Unit = {
          out.println("Successfully parsed. Type `:ast` or `:generate json` or `:generate yaml`")
          callback(Some(Document(unit.asInstanceOf[amf.document.Document])))
        }
        override def error(exception: Throwable): Unit = {
          callback(None)
          out.println(exception)
        }
      }
    )
  }

  private object Json {
    def unapply(line: String): Option[String] = {
      line match {
        case s if s.startsWith(":json ") => Some(s.stripPrefix(":json "))
        case _                           => None
      }
    }
  }

  private object Yaml {
    def unapply(line: String): Option[String] = {
      line match {
        case s if s.startsWith(":yaml ") => Some(s.stripPrefix(":yaml "))
        case _                           => None
      }
    }
  }

  private object Ast {
    def unapply(line: String): Option[String] = {
      line match {
        case s if s.startsWith(":ast") => Some(line)
        case _                         => None
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
