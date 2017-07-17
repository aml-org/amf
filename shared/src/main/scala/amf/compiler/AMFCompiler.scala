package amf.compiler

import amf.common.{AMFAST, AMFToken}
import amf.document.{BaseUnit, Document}
import amf.exception.CyclicReferenceException
import amf.json.JsonLexer
import amf.lexer.AbstractLexer
import amf.maker.WebApiMaker
import amf.oas.OASParser
import amf.parser.{BaseAMFParser, YeastASTBuilder}
import amf.raml.RamlParser
import amf.remote.Mimes._
import amf.remote.Syntax.{Json, Yaml}
import amf.remote._
import amf.yaml.YamlLexer

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
  private var ast: AMFAST                              = _
  private val references: ListBuffer[Future[BaseUnit]] = ListBuffer()

  def build(): Future[BaseUnit] = {
    if (context.hasCycles) failed(new CyclicReferenceException(context.history))
    else {
      cache.getOrUpdate(location) {
        compile
      }
    }
  }

  private def compile() = root().map(build)

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
          case _    => ???
        }
    }
  }

  def resolveParser(builder: YeastASTBuilder, content: Content): BaseAMFParser = {
    content.mime match {
      case Some(`APPLICATION/RAML` | `APPLICATION/RAML+JSON` | `APPLICATION/RAML+YAML`) => new RamlParser(builder)
      case Some(
          `APPLICATION/OPENAPI+JSON` | `APPLICATION/SWAGGER+JSON` | `APPLICATION/OPENAPI+YAML` |
          `APPLICATION/SWAGGER+YAML` | `APPLICATION/OPENAPI` | `APPLICATION/SWAGGER`) =>
        new OASParser(builder)
      case _ =>
        hint.vendor match {
          case Raml => new RamlParser(builder)
          case Oas  => new OASParser(builder)
          case _    => new RamlParser(builder)
        }
    }
  }

  private def build(root: Root): BaseUnit = {
    root match {
      case Root(_, _, _, Oas)  => createOasDocument(root)
      case Root(_, _, _, Raml) => createRamlDocument(root)
    }
  }

  private def createRamlDocument(root: Root): Document = {
    hint.kind match {
      case Library     => Document(root.location, root.references, WebApiMaker(root).make) // TODO libraries
      case Link        => Document(root.location, root.references, WebApiMaker(root).make) // TODO includes
      case Unspecified => Document(root.location, root.references, WebApiMaker(root).make)
    }
  }

  private def createOasDocument(root: Root): Document = {
    Document(root.location, root.references, WebApiMaker(root).make)
  }

  private def parse(content: Content) = {
    val lexer   = resolveLexer(content)
    val builder = YeastASTBuilder(lexer)
    val parser  = resolveParser(builder, content)

    if (ast == null) {
      ast = builder.root() {
        parser.parse
      }
    }

    builder.references.foreach(link => {
      references += link.resolve(remote, context, cache, hint)
    })

    Future.sequence(references).map(rs => Root(ast, URL(content.url), rs.map(_.location()), parser.vendor()))
  }
}

case class Root(ast: AMFAST, location: URL, references: Seq[URL], vendor: Vendor)

object AMFCompiler {
  def apply(url: String, remote: Platform, hint: Hint, context: Option[Context] = None, cache: Option[Cache] = None) =
    new AMFCompiler(url, remote, context, hint, cache.getOrElse(Cache()))
}
