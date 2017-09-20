package amf.compiler

import amf.common.AMFToken
import amf.document.{BaseUnit, Document}
import amf.domain.extensions.idCounter
import amf.exception.{CyclicReferenceException, UnableToResolveLexerException}
import amf.graph.GraphParser
import amf.json.JsonLexer
import amf.lexer.AbstractLexer
import amf.parser.YeastASTBuilder
import amf.remote.Mimes._
import amf.remote.Syntax.{Json, Yaml}
import amf.remote._
import amf.spec.oas.OasSpecParser
import amf.spec.raml.RamlSpecParser
import amf.yaml.YamlLexer
import org.yaml.model.YDocument
import org.yaml.parser.YamlParser

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.failed

class AMFCompiler private (val url: String,
                           val remote: Platform,
                           val base: Option[Context],
                           hint: Hint,
                           private val cache: Cache) {

  private lazy val context: Context                    = base.map(_.update(url)).getOrElse(Context(remote, url))
  private lazy val location                            = context.current
  private val references: ListBuffer[Future[BaseUnit]] = ListBuffer()

  def build(): Future[BaseUnit] = {
    // Reset the data node counter
    idCounter.reset()

    if (context.hasCycles) failed(new CyclicReferenceException(context.history))
    else
      cache.getOrUpdate(location) { () =>
        compile()
      }
  }

  private def compile() = root().map(make)

  /** Usage at tests. */
  private[compiler] def root(): Future[Root] = {
    remote
      .resolve(location, base)
      .flatMap(parse)
  }

  def resolveLexer(content: Content): AbstractLexer[AMFToken] = {
    content.mime match {
      case Syntax(Yaml) => YamlLexer(content.stream)
      case Syntax(Json) => JsonLexer(content.stream)
      case _ =>
        hint.syntax match {
          case Yaml => YamlLexer(content.stream)
          case Json => JsonLexer(content.stream)
          case _    => throw new UnableToResolveLexerException
        }
    }
  }

  def resolveVendor(content: Content): Vendor = {
    content.mime match {
      case Some(`APPLICATION/RAML` | `APPLICATION/RAML+JSON` | `APPLICATION/RAML+YAML`) => Raml
      case Some(
          `APPLICATION/OPENAPI+JSON` | `APPLICATION/SWAGGER+JSON` | `APPLICATION/OPENAPI+YAML` |
          `APPLICATION/SWAGGER+YAML` | `APPLICATION/OPENAPI` | `APPLICATION/SWAGGER`) =>
        Oas
      case _ => hint.vendor
    }
  }

  private def make(root: Root): BaseUnit = {
    root match {
      case Root(_, _, _, Amf)  => makeAmfUnit(root)
      case Root(_, _, _, Raml) => makeRamlUnit(root)
      case Root(_, _, _, Oas)  => makeOasUnit(root)
    }
  }

  private def makeRamlUnit(root: Root): BaseUnit = {
    hint.kind match {
      case Library     => makeDocument(root) // TODO libraries
      case Link        => makeDocument(root) // TODO includes
      case Unspecified => resolveRamlUnit(root)
    }
  }

  private def resolveRamlUnit(root: Root) = {
    //todo syaml read comment
//    root.document.head match {
//      case c if c.is(Comment) && RAML_10 == c.content => makeDocument(root)
//      case _                                          => throw new UnableToResolveUnitException
//    }
    makeDocument(root)
  }

  private def makeDocument(root: Root): Document = {
    root.vendor match {
      case Raml => RamlSpecParser(root).parseDocument()
      case Oas  => OasSpecParser(root).parseDocument()
      case _    => throw new IllegalStateException(s"Invalid vendor ${root.vendor}")
    }
  }

  private def makeOasUnit(root: Root): BaseUnit = {
    //todo syaml read comment
    //    root.document.head.children.find(e =>
//      e.is(Entry) && e.head.content.unquote == "swagger" && e.last.content.unquote == "2.0") match {
//      case Some(_) => makeDocument(root)
//      case _       => throw new UnableToResolveUnitException
//    }
    makeDocument(root)
  }

  private def makeAmfUnit(root: Root): BaseUnit = GraphParser.parse(root.document, root.location)

  private def parse(content: Content): Future[Root] = {
    val lexer   = resolveLexer(content)
    val builder = YeastASTBuilder(lexer, content.url)

    val parser = YamlParser(content.stream.toString)

    val document = parser.parse(true) collectFirst { case d: YDocument => d }

    document match {
      case Some(d) =>
        val vendor = resolveVendor(content)
        val refs   = new ReferenceCollector(d, vendor).traverse()

        refs.foreach(link => {
          references += link.resolve(remote, context, cache, hint)
        })

        Future.sequence(references).map(rs => { Root(d, content.url, rs, vendor) })
      case None => Future.failed(new Exception("Unable to parse document."))
    }
  }

  private def collectReferences(document: YDocument, vendor: Vendor) = {
    document
  }
}

case class Root(document: YDocument, location: String, references: Seq[BaseUnit], vendor: Vendor)

object AMFCompiler {
  def apply(url: String, remote: Platform, hint: Hint, context: Option[Context] = None, cache: Option[Cache] = None) =
    new AMFCompiler(url, remote, context, hint, cache.getOrElse(Cache()))

  val RAML_10 = "#%RAML 1.0\n"
}
