package amf.compiler

import amf.dialects.DialectRegistry
import amf.document.BaseUnit
import amf.document.Fragment.ExternalFragment
import amf.domain.ExternalDomainElement
import amf.domain.extensions.idCounter
import amf.exception.CyclicReferenceException
import amf.framework.parser.ReferenceKind
import amf.parser.{YNodeLikeOps, YScalarYRead}
import amf.plugins.domain.graph.AMFGraphPlugin
import amf.plugins.domain.graph.parser.GraphParser
import amf.plugins.domain.payload.PayloadPlugin
import amf.plugins.domain.payload.parser.PayloadParser
import amf.plugins.domain.vocabularies.RAMLExtensionsPlugin
import amf.plugins.domain.webapi.contexts.WebApiContext
import amf.plugins.domain.webapi.{OAS20Plugin, RAML10Plugin}
import amf.remote.Mimes._
import amf.remote._
import amf.spec.ParserContext
import amf.validation.Validation
import org.yaml.model._
import org.yaml.parser.YamlParser

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.failed

class AMFCompiler private (val url: String,
                           val remote: Platform,
                           val base: Option[Context],
                           hint: Hint,
                           val currentValidation: Validation,
                           private val cache: Cache,
                           private val dialects: amf.dialects.DialectRegistry = amf.dialects.DialectRegistry.default,
                           private val baseContext: Option[ParserContext] = None) {

  implicit val ctx: ParserContext = baseContext.getOrElse(ParserContext(currentValidation, url, Seq.empty))

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
      case Root(_, _, _, hint.kind, Amf, _)     => makeAmfUnit(root)
      case Root(_, _, _, hint.kind, Raml, _)    => makeRamlUnit(root)
      case Root(_, _, _, hint.kind, Oas, _)     => makeOasUnit(root)
      case Root(_, _, _, hint.kind, Payload, _) => makePayloadUnit(root)
      case Root(_, _, _, hint.kind, Unknown, _) => makeExternalUnit(root)
    }
  }

  private def makeExternalUnit(root: Root): BaseUnit = {
    val external = ExternalDomainElement().withRaw(root.raw)
    external.adopted(root.location)
    ExternalFragment().withLocation(root.location).withEncodes(external)
  }

  val raml10plugin = new RAML10Plugin()
  val dialectsplugin = new RAMLExtensionsPlugin(dialects)

  private def makeRamlUnit(root: Root): BaseUnit = {
    implicit val ctx: ParserContext = ParserContext(currentValidation, root.location, root.references, Some(ctx.declarations)).toRaml

    val parsed: Option[BaseUnit] = if (raml10plugin.accept(root)) {
      raml10plugin.parse(root, ctx)
    } else if (dialectsplugin.accept(root)) {
      dialectsplugin.parse(root, ctx)
    } else {
      None
    }

    parsed match {
      case Some(baseUnit) => baseUnit
      case None           => makeExternalUnit(root)
    }
  }

  private def makeOasUnit(root: Root): BaseUnit = {
    val ctx: ParserContext = ParserContext(currentValidation, root.location, root.references, Some(ctx.declarations))
    new OAS20Plugin().parse(root, ctx) match {
      case Some(unit) => unit
      case None       => makeExternalUnit(root)
    }
  }

  private def makeAmfUnit(root: Root): BaseUnit = {
    val graphPlugin = new AMFGraphPlugin(remote)
    if (graphPlugin.accept(root)) {
      graphPlugin.parse(root, ctx) match {
        case Some(baseUnit) => baseUnit
        case _ => throw new Exception("Cannot parse AMF JSON-LD graph")
      }
    } else {
      throw new Exception("Cannot parse AMF JSON-LD graph")
    }
  }

  private def makePayloadUnit(root: Root): BaseUnit = {
    implicit val ctx: WebApiContext = ParserContext(currentValidation, root.location, root.references, Some(ctx.declarations)).toRaml
    new PayloadPlugin().parse(root, ctx) match {
      case Some(baseUnit) => baseUnit
      case _ => throw new Exception("Cannot parse AMF Payload document")
    }
  }

  private def parse(content: Content): Future[Root] = {
    val raw    = content.stream.toString
    val parser = YamlParser(raw)

    val parsed = toDocument(parser.parse(true))

    parsed match {
      case Some(document) =>
        document.document.tagType match {
          // Payloads array
          case YType.Seq if hint == PayloadJsonHint || hint == PayloadYamlHint =>
            Future(Root(document, content.url, Seq(), hint.kind, Payload, raw))

          // AMF JSON-LD with a single element in array
          case YType.Seq if hint == AmfJsonHint && document.document.as[Seq[YNode]].length == 1 =>
            parseDoc(content, document, raw)

          case YType.Map | YType.Seq =>
            parseDoc(content, document, raw)

          // Payloads scalar
          case _ if document.document.toOption[YScalar].isDefined =>
            if (hint == PayloadJsonHint || hint == PayloadYamlHint)
              Future(Root(document, content.url, Seq(), hint.kind, Payload, raw))
            else Future(Root(document, content.url, Seq(), hint.kind, Unknown, raw))
          case _ => Future.failed(new Exception("Unable to parse document."))
        }
      case None => Future.failed(new Exception("Unable to parse document."))
    }
  }

  private def parseDoc(content: Content, document: ParsedDocument, raw: String) = {
    val vendor = resolveVendor(content)
    // construct local parser contxt and pass to referencecollector as explicit because we don't know the vendor yet
    val refs =
      new ReferenceCollector(document, vendor, currentValidation).traverse()

    refs
      .filter(_.isRemote)
      .foreach(link => {
        references += link
          .resolve(remote, context, cache, hint, currentValidation, dialects, ctx)
          .map(r => ParsedReference(r, link.url, hint.kind))
      })

    Future.sequence(references).map(rs => { Root(document, content.url, rs, hint.kind, vendor, raw) })
  }

  private def toDocument(parts: Seq[YPart]) = {
    if (parts.exists(v => v.isInstanceOf[YDocument])) {
      parts collectFirst { case d: YDocument => d } map { document =>
        val comment = parts collectFirst { case c: YComment => c }
        ParsedDocument(comment, document)
      }
    } else {
      parts collectFirst { case d: YComment => d } map { comment =>
        ParsedDocument(Some(comment), YDocument(IndexedSeq(YNode(YMap()))))
      }
    }
  }
}

case class Root(parsed: ParsedDocument,
                location: String,
                references: Seq[ParsedReference],
                referenceKind: ReferenceKind,
                vendor: Vendor,
                raw: String) {
  val document: YDocument = parsed.document
}

case class ParsedDocument(comment: Option[YComment], document: YDocument)

case class ParsedReference(baseUnit: BaseUnit, parsedUrl: String, referenceKind: ReferenceKind)

object AMFCompiler {
  def apply(url: String,
            remote: Platform,
            hint: Hint,
            currentValidation: Validation,
            context: Option[Context] = None,
            cache: Option[Cache] = None,
            dialects: DialectRegistry = DialectRegistry.default,
            ctx: Option[ParserContext] = None
  ) = new AMFCompiler(url, remote, context, hint, currentValidation, cache.getOrElse(Cache()), dialects, ctx)

  val RAML_10 = "#%RAML 1.0\n"
}
