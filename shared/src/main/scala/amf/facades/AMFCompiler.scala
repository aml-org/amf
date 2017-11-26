package amf.facades

import amf.core
import amf.core.{AMFCompiler => ModularCompiler}

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future

class AMFCompiler private (val url: String,
                           val remote: Platform,
                           val base: Option[Context],
                           hint: Hint,
                           val currentValidation: Validation,
                           private val cache: Cache,
                           private val baseContext: Option[ParserContext] = None) extends RamlHeaderExtractor {

  implicit val ctx: ParserContext = baseContext.getOrElse(ParserContext(url, Seq.empty))
  private lazy val context: Context                           = base.map(_.update(url)).getOrElse(core.remote.Context(remote, url))
  private lazy val location                                   = context.current
  private val references: ListBuffer[Future[ParsedReference]] = ListBuffer()

  def build(): Future[BaseUnit] = {

    val actualVendor = hint.vendor match {
      case Raml      => "RAML 1.0"
      case Oas       => "OAS 2.0"
      case Payload   => "AMF Payload"
      case Amf       => "AMF Graph"
      case Extension => "RAML Extension"
      case Unknown   => "Unknown Vendor"
    }

    val mediaType = hint match {
      case RamlYamlHint => "application/yaml"
      case RamlJsonHint => "application/json"
      case OasJsonHint  => "application/json"
      case OasYamlHint  => "application/yaml"
      case AmfJsonHint  => "application/ld+json"
      case PayloadJsonHint => "application/amf+json"
      case PayloadYamlHint => "application/amf+yaml"
      case ExtensionYamlHint => "application/yaml"
      case _            => "text/plain"
    }

    new ModularCompiler(
      url,
      remote,
      base,
      mediaType,
      actualVendor,
      hint.kind,
      cache,
      baseContext
    ).build()

    /*
    // Reset the data node counter
    idCounter.reset()

    if (context.hasCycles) failed(new CyclicReferenceException(context.history))
    else
      cache.getOrUpdate(location) { () =>
        compile()
      }
      */
  }

  def root(): Future[Root] = {
    val actualVendor = hint.vendor match {
      case Raml    => "RAML 1.0"
      case Oas     => "OAS 2.0"
      case Payload => "AMF Payload"
      case Amf     => "AMF Graph"
      case Extension => "AMF Extension"
      case Unknown => "Unknown Vendor"
    }

    val mediaType = hint match {
      case RamlYamlHint => "application/yaml"
      case RamlJsonHint => "application/json"
      case OasJsonHint  => "application/json"
      case OasYamlHint  => "application/yaml"
      case AmfJsonHint  => "application/ld+json"
      case ExtensionYamlHint => "application/raml"
      case _            => "text/plain"
    }

    new ModularCompiler(
      url,
      remote,
      base,
      mediaType,
      actualVendor,
      hint.kind,
      cache,
      Some(ctx)
    ).root() map { case root => root.oldFormat() }
  }
/*
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
  */
}

case class Root(parsed: ParsedDocument,
                location: String,
                references: Seq[ParsedReference],
                referenceKind: ReferenceKind,
                vendor: Vendor,
                raw: String) {
  val document: YDocument = parsed.document

  // TODO: remove me, only for compatibility while refactoring
  def newFormat(): amf.core.Root = {
    val actualVendor = vendor match {
      case Raml    => "RAML 1.0"
      case Oas     => "OAS 2.0"
      case Payload => "AMF Payload"
      case Amf     => "AMF Graph"
      case Extension => "AMF Extension"
      case Unknown => "Unknown Vendor"
    }
    val mediatype = vendor match {
      case Extension => "application/yaml"
      case Raml    => "application/yaml"
      case Oas     => "application/json"
      case Payload => "application/amf+json"
      case Amf     => "application/ld+json"
      case Unknown => "text/plain"
    }

    amf.core.Root(
      parsed,
      location,
      mediatype,
      references,
      referenceKind,
      actualVendor,
      raw
    )
  }
}

object AMFCompiler {
  def apply(url: String,
            remote: Platform,
            hint: Hint,
            currentValidation: Validation,
            context: Option[Context] = None,
            cache: Option[Cache] = None,
            ctx: Option[ParserContext] = None) =
    new AMFCompiler(url, remote, context, hint, currentValidation, cache.getOrElse(Cache()))

  val RAML_10 = "#%RAML 1.0\n"
}
