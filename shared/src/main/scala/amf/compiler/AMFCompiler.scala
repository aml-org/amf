package amf.compiler

import amf.document.BaseUnit
import amf.domain.extensions.idCounter
import amf.exception.{CyclicReferenceException, UnableToResolveUnitException}
import amf.graph.GraphParser
import amf.remote.Mimes._
import amf.remote._
import amf.spec.oas.{OasDocumentParser, OasModuleParser}
import amf.spec.raml.{RamlDocumentParser, RamlModuleParser}
import org.yaml.model.{YComment, YDocument, YPart}
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

  private lazy val context: Context                           = base.map(_.update(url)).getOrElse(Context(remote, url))
  private lazy val location                                   = context.current
  private val references: ListBuffer[Future[ParsedReference]] = ListBuffer()

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
    val option = RamlHeader(root).map({
      case RamlHeader.Raml10        => RamlDocumentParser(root).parseDocument()
      case RamlHeader.Raml10Library => RamlModuleParser(root).parseModule()
      case _                        => throw new UnableToResolveUnitException
    })
    option match {
      case Some(unit) => unit
      case None       => throw new UnableToResolveUnitException
    }
  }

  private def makeOasUnit(root: Root): BaseUnit = {
    val option = OasHeader(root).map({
      case OasHeader.Oas_20 => resolveOasUnit(root: Root)
      case _                => throw new UnableToResolveUnitException
    })
    option match {
      case Some(unit) => unit
      case None       => throw new UnableToResolveUnitException
    }
  }

  private def resolveOasUnit(root: Root): BaseUnit = {
    hint.kind match {
      case Library => OasModuleParser(root).parseModule()
      case _       => OasDocumentParser(root).parseDocument()
    }
  }

  private def makeAmfUnit(root: Root): BaseUnit = GraphParser.parse(root.document, root.location)

  private def parse(content: Content): Future[Root] = {
    val parser = YamlParser(content.stream.toString)

    val parsed = toDocument(parser.parse(true))

    parsed match {
      case Some(document) =>
        val vendor = resolveVendor(content)
        val refs   = new ReferenceCollector(document.document, vendor).traverse()

        refs
          .filter(_.isRemote)
          .foreach(link => {
            references += link.resolve(remote, context, cache, hint).map(r => ParsedReference(r, link.url))
          })

        Future.sequence(references).map(rs => { Root(document, content.url, rs, vendor) })
      case None => Future.failed(new Exception("Unable to parse document."))
    }
  }

  private def toDocument(parts: Seq[YPart]) = {
    parts collectFirst { case d: YDocument => d } map { document =>
      val comment = parts collectFirst { case c: YComment => c }
      ParsedDocument(comment, document)
    }
  }
}

case class Root(parsed: ParsedDocument, location: String, references: Seq[ParsedReference], vendor: Vendor) {
  val document: YDocument = parsed.document
}

case class ParsedDocument(comment: Option[YComment], document: YDocument)

case class ParsedReference(baseUnit: BaseUnit, parsedUrl: String)

object AMFCompiler {
  def apply(url: String, remote: Platform, hint: Hint, context: Option[Context] = None, cache: Option[Cache] = None) =
    new AMFCompiler(url, remote, context, hint, cache.getOrElse(Cache()))

  val RAML_10 = "#%RAML 1.0\n"
}
