package amf.compiler

import amf.common.{AMFAST, AMFToken}
import amf.exception.CyclicReferenceException
import amf.json.JsonLexer
import amf.lexer.AbstractLexer
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
                           hint: Option[Hint],
                           private val cache: Cache) {

  private lazy val context: Context                            = base.map(_.update(url)).getOrElse(Context(remote, url))
  private var root: AMFAST                                     = _
  private val references: ListBuffer[Future[(AMFAST, Vendor)]] = ListBuffer()

  def build(): Future[(AMFAST, Vendor)] = {
    val url = context.current

    if (context.hasCycles) failed(new CyclicReferenceException(context.history))
    else {
      cache.getOrUpdate(url) { () =>
        remote.resolve(url, base).flatMap(parse)
      }
    }
  }

  def resolveLexer(content: Content): AbstractLexer[AMFToken] = {
    content.mime match {
      case Syntax(Yaml) => YamlLexer(content.stream)
      case Syntax(Json) => JsonLexer(content.stream)
      case _ =>
        hint.map(_.syntax) match {
          case Some(Yaml) => YamlLexer(content.stream)
          case Some(Json) => JsonLexer(content.stream)
          case _          => ???
        }
    }
  }

  def resolveParser(builder: YeastASTBuilder, content: Content): BaseAMFParser = {
    //TODO
    builder.currentText match {
      case s if s.startsWith("#%RAML 1.0") => new RamlParser(builder)
      case _ =>
        content.mime match {
          case Some(`APPLICATION/RAML`) | Some(`APPLICATION/RAML+JSON`) | Some(`APPLICATION/RAML+YAML`) =>
            new RamlParser(builder)
          case Some(`APPLICATION/OPENAPI+JSON`) | Some(`APPLICATION/SWAGGER+JSON`) | Some(`APPLICATION/OPENAPI+YAML`) |
              Some(`APPLICATION/SWAGGER+YAML`) | Some(`APPLICATION/OPENAPI`) | Some(`APPLICATION/SWAGGER`) =>
            new OASParser(builder)
          case _ =>
            hint.getOrElse("") match {
              case RamlYamlHint | RamlJsonHint => new RamlParser(builder)
              case OasYamlHint | OasJsonHint   => new OASParser(builder)
              case _                           => new RamlParser(builder)
            }
        }
    }
  }

  private def parse(content: Content): Future[(AMFAST, Vendor)] = {
    val builder = YeastASTBuilder(resolveLexer(content))
    val parser  = resolveParser(builder, content)

    if (root == null) {
      root = builder.root() {
        parser.parse
      }
    }

    builder.references.foreach(link => {
      references += link.resolve(remote, context, cache, hint)
    })

    Future.sequence(references).map(_ => (root, parser.vendor()))
  }
}

object AMFCompiler {
  def apply(url: String,
            remote: Platform,
            hint: Option[Hint],
            context: Option[Context] = None,
            cache: Option[Cache] = None) =
    new AMFCompiler(url, remote, context, hint, cache.getOrElse(Cache()))
}
