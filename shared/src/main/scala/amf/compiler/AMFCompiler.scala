package amf.compiler

import amf.compiler.OasHeader.{Oas20Extension, Oas20Header, Oas20Overlay}
import amf.compiler.RamlHeader.{Raml10Extension, Raml10Overlay}
import amf.dialects.DialectRegistry
import amf.document.BaseUnit
import amf.document.Fragment.ExternalFragment
import amf.domain.ExternalDomainElement
import amf.domain.extensions.idCounter
import amf.exception.{CyclicReferenceException, UnableToResolveUnitException}
import amf.graph.GraphParser
import amf.remote.Mimes._
import amf.remote._
import amf.spec.ParserContext
import amf.spec.dialects.DialectParser
import amf.spec.oas.{OasDocumentParser, OasFragmentParser, OasModuleParser}
import amf.spec.payload.PayloadParser
import amf.spec.raml.{RamlDocumentParser, RamlFragmentParser, RamlModuleParser}
import amf.validation.Validation
import org.yaml.model._
import org.yaml.parser.YamlParser

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.failed
import amf.parser.{YNodeLikeOps, YScalarYRead}

class AMFCompiler private (val url: String,
                           val remote: Platform,
                           val base: Option[Context],
                           hint: Hint,
                           val currentValidation: Validation,
                           private val cache: Cache,
                           private val dialects: amf.dialects.DialectRegistry = amf.dialects.DialectRegistry.default) {

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
      case Root(_, _, _, Amf, _)     => makeAmfUnit(root)
      case Root(_, _, _, Raml, _)    => makeRamlUnit(root)
      case Root(_, _, _, Oas, _)     => makeOasUnit(root)
      case Root(_, _, _, Payload, _) => makePayloadUnit(root)
      case Root(_, _, _, Unknown, _) => makeExternalUnit(root)
    }
  }

  private def makeExternalUnit(root: Root): BaseUnit = {
    val external = ExternalDomainElement().withRaw(root.raw)
    external.adopted(root.location)
    ExternalFragment().withLocation(root.location).withEncodes(external)
  }

  private def makeRamlUnit(root: Root): BaseUnit = {
    implicit val ctx: ParserContext = ParserContext(currentValidation, Raml, root.references)
    val option = RamlHeader(root).map {
      case RamlHeader.Raml10          => RamlDocumentParser(root).parseDocument()
      case RamlHeader.Raml10Overlay   => RamlDocumentParser(root).parseOverlay()
      case RamlHeader.Raml10Extension => RamlDocumentParser(root).parseExtension()
      case RamlHeader.Raml10Library   => RamlModuleParser(root).parseModule()
      case fragment: RamlFragment     => RamlFragmentParser(root, fragment).parseFragment()
      // this includes vocabularies and dialect definitions and dialect documents
      // They are all defined internally in terms of dialects definitions
      case header if dialects.knowsHeader(header) => makeDialect(root, header)
      case _                                      => throw UnableToResolveUnitException(root.location)
    }
    option match {
      case Some(unit) => unit
      case None       => makeExternalUnit(root)
    }
  }

  private def makeOasUnit(root: Root): BaseUnit = resolveOasUnit(root)

  private def resolveOasUnit(root: Root): BaseUnit = {
    implicit val ctx: ParserContext = ParserContext(currentValidation, Oas, root.references)
    hint.kind match {
      case Library => OasModuleParser(root).parseModule()
      case Link    => OasFragmentParser(root).parseFragment()
      case _       => detectOasUnit(root)
    }
  }

  private def detectOasUnit(root: Root)(implicit ctx: ParserContext): BaseUnit = {
    OasHeader(root) match {
      case Some(Oas20Overlay)   => OasDocumentParser(root).parseOverlay()
      case Some(Oas20Extension) => OasDocumentParser(root).parseExtension()
      case Some(Oas20Header)    => OasDocumentParser(root).parseDocument()
      case f if f.isDefined     => OasFragmentParser(root, f).parseFragment()
      case _                    => throw UnableToResolveUnitException(root.location)
    }
  }

  private def makeDialect(root: Root, header: RamlHeader)(implicit ctx: ParserContext): BaseUnit =
    DialectParser(root, header, dialects).parseUnit()

  private def makeAmfUnit(root: Root): BaseUnit = GraphParser(remote).parse(root.document, root.location)

  private def makePayloadUnit(root: Root): BaseUnit = {
    implicit val ctx: ParserContext = ParserContext(currentValidation, Payload, root.references)
    PayloadParser(root.document, root.location).parseUnit()
  }

  // TODO take this away when dialects don't use 'extends' keyword.
  def isRamlOverlayOrExtension(vendor: Vendor, document: ParsedDocument): Boolean = {
    document.comment match {
      case Some(c) =>
        RamlHeader.fromText(c.metaText) match {
          case Some(Raml10Overlay | Raml10Extension) if vendor == Raml => true
          case _                                                       => false
        }
      case None => false
    }
  }

  private def parse(content: Content): Future[Root] = {
    val raw    = content.stream.toString
    val parser = YamlParser(raw)

    val parsed = toDocument(parser.parse(true))

    parsed match {
      case Some(document) =>
        document.document.tagType match {
          case YType.Map =>
            parseDoc(content, document, raw)

          // Payloads array
          case YType.Seq if hint == PayloadJsonHint || hint == PayloadYamlHint =>
            Future(Root(document, content.url, Seq(), Payload, raw))

          // AMF JSON-LD with a single element in array
          case YType.Seq if hint == AmfJsonHint && document.document.as[Seq[YNode]].length == 1 =>
            parseDoc(content, document, raw)

          // Payloads scalar
          case _ if document.document.toOption[YScalar].isDefined =>
            if (hint == PayloadJsonHint || hint == PayloadYamlHint)
              Future(Root(document, content.url, Seq(), Payload, raw))
            else Future(Root(document, content.url, Seq(), Unknown, raw))

          case _ => Future.failed(new Exception("Unable to parse document."))
        }
      case None => Future.failed(new Exception("Unable to parse document."))
    }
  }

  private def parseDoc(content: Content, document: ParsedDocument, raw: String) = {
    val vendor = resolveVendor(content)
    // construct local parser contxt and pass to referencecollector as explicit because we don't know the vendor yet
    val refs =
      new ReferenceCollector(document.document, vendor, currentValidation)
        .traverse(isRamlOverlayOrExtension(vendor, document))

    refs
      .filter(_.isRemote)
      .foreach(link => {
        references += link
          .resolve(remote, context, cache, hint, currentValidation, dialects)
          .map(r => ParsedReference(r, link.url))
      })

    Future.sequence(references).map(rs => { Root(document, content.url, rs, vendor, raw) })
  }

  private def toDocument(parts: Seq[YPart]) = {
    if (parts.find(v => v.isInstanceOf[YDocument]).isDefined) {
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
                vendor: Vendor,
                raw: String) {
  val document: YDocument = parsed.document
}

case class ParsedDocument(comment: Option[YComment], document: YDocument)

case class ParsedReference(baseUnit: BaseUnit, parsedUrl: String)

object AMFCompiler {
  def apply(url: String,
            remote: Platform,
            hint: Hint,
            currentValidation: Validation,
            context: Option[Context] = None,
            cache: Option[Cache] = None,
            dialects: DialectRegistry = DialectRegistry.default) =
    new AMFCompiler(url, remote, context, hint, currentValidation, cache.getOrElse(Cache()), dialects)

  val RAML_10 = "#%RAML 1.0\n"
}
