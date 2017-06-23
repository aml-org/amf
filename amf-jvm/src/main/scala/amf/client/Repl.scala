package amf.client

import java.io.{InputStream, PrintStream}
import java.util.Scanner

import amf.generator.{JsonGenerator, YamlGenerator}
import amf.parser.{ASTNodePrinter, Document}
import amf.remote.{Hint, OasJsonHint, RamlYamlHint}

class Repl(val in: InputStream, val out: PrintStream) {

  init()

  private def init(): Unit = {
    val scanner                    = new Scanner(in)
    var document: Option[Document] = None

    while (scanner.hasNextLine) {
      scanner.nextLine() match {
        case Exit()           => return
        case Json(url)        => remote(url, Option(OasJsonHint), (doc) => document = doc)
        case Yaml(url)        => remote(url, Option(RamlYamlHint), (doc) => document = doc)
        case Ast(_)           => document.foreach(doc => out.println(ASTNodePrinter.print(doc.root)))
        case Generate(syntax) => document.foreach(doc => generate(doc, syntax))
        case line             => out.println(s"... $line")
      }
    }
  }

  private def generate(document: Document, syntax: String): Unit = {
    syntax match {
      case "json" => out.println(new JsonGenerator().generate(document.root))
      case "yaml" => out.println(new YamlGenerator().generate(document.root))
      case _      => out.println(s"Unsupported generation for: $syntax")
    }
  }

  private def remote(url: String, hint: Option[Hint], callback: (Option[Document]) => Unit): Unit = {
    new JvmClient().generate(
      url,
      hint,
      new Handler {
        override def success(doc: Document): Unit = {
          out.println("Successfully parsed. Type `:ast` or `:generate json` or `:generate yaml`")
          callback(Some(doc))
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
