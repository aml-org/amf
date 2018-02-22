package amf.core

import amf.core
import amf.core.exception.CyclicReferenceException
import amf.core.model.document.{BaseUnit, ExternalFragment}
import amf.core.model.domain.ExternalDomainElement
import amf.core.parser.{ParsedDocument, ParsedReference, ParserContext, ReferenceKind}
import amf.core.plugins.AMFDocumentPlugin
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote._
import amf.core.services.RuntimeCompiler

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.failed

class AMFCompiler(val rawUrl: String,
                  val remote: Platform,
                  val base: Option[Context],
                  val mediaType: Option[String],
                  val vendor: String,
                  val referenceKind: ReferenceKind,
                  private val cache: Cache,
                  private val baseContext: Option[ParserContext] = None) {

  val url                                                     = new java.net.URI(rawUrl).normalize().toString
  private lazy val context: Context                           = base.map(_.update(url)).getOrElse(core.remote.Context(remote, url))
  private lazy val location                                   = context.current
  private val references: ListBuffer[Future[ParsedReference]] = ListBuffer()
  private val ctx: ParserContext                              = baseContext.getOrElse(ParserContext(url))

  def build(): Future[BaseUnit] = {
    if (context.hasCycles) failed(new CyclicReferenceException(context.history))
    else
      cache.getOrUpdate(location) { () =>
        compile()
      }
  }

  private def compile() = resolve().map(parseSyntax).flatMap(parseDomain)

  private def parseSyntax(inputContent: Content): Either[Content, Root] = {

    val content = AMFPluginsRegistry.featurePlugins().foldLeft(inputContent) { case (content, plugin) =>
      plugin.onBeginDocumentParsing(url, content, referenceKind, vendor)
    }

    val parsed = content.mime
      .orElse(mediaType)
      .flatMap(mime => AMFPluginsRegistry.syntaxPluginForMediaType(mime).flatMap(_.parse(mime, content.stream, ctx)))
      // if we cannot find a plugin with the resolved media type, we try parsing from file extension
      .orElse {
        FileMediaType
          .extension(content.url)
          .flatMap(FileMediaType.mimeFromExtension)
          .flatMap(mime =>
            AMFPluginsRegistry.syntaxPluginForMediaType(mime).flatMap(_.parse(mime, content.stream, ctx)))
      }

    parsed match {
      case Some(inputDocument) =>
        val document = AMFPluginsRegistry.featurePlugins().foldLeft(inputDocument) { case (doc, plugin) =>
          plugin.onSyntaxParsed(url, doc)
        }
        Right(
          Root(document,
               content.url,
               content.mime.getOrElse(mediaType.getOrElse("")),
               Seq(),
               referenceKind,
               vendor,
               content.stream.toString))
      case None =>
        Left(content)
    }
  }

  def parseExternalFragment(content: Content): Future[BaseUnit] = {
    val result = ExternalDomainElement().withRaw(content.stream.toString) //
    content.mime.foreach(mime => result.withMediaType(mime))
    Future.successful(ExternalFragment().withId(content.url).withEncodes(result).withLocation(content.url))
  }

  private def parseDomain(parsed: Either[Content, Root]): Future[BaseUnit] = {
    parsed match {
      case Left(content)   => parseExternalFragment(content)
      case Right(document) => parseDomain(document)
    }
  }

  private def parseDomain(document: Root): Future[BaseUnit] = {
    val domainPluginOption = AMFPluginsRegistry.documentPluginForVendor(vendor).find(_.canParse(document)) match {
      case Some(domainPlugin) => Some(domainPlugin)
      case None               => AMFPluginsRegistry.documentPluginForMediaType(document.mediatype).find(_.canParse(document))
    }

    val futureDocument = domainPluginOption match {
      case Some(domainPlugin) =>
        parseReferences(document, domainPlugin) map { documentWithReferences =>
          domainPlugin.parse(documentWithReferences, ctx, remote) match {
            case Some(baseUnit) =>
              baseUnit.withRaw(document.raw)
            case None =>
              ExternalFragment()
                .withEncodes(
                  ExternalDomainElement()
                    .withRaw(document.raw)
                    .withMediaType(document.mediatype))
          }
        }
      case None =>
        val fragment = ExternalFragment()
          .withId(document.location)
          .withEncodes(ExternalDomainElement().withRaw(document.raw).withMediaType(document.mediatype))
        Future.successful(fragment)
    }

    futureDocument map { baseUnit: BaseUnit =>
      AMFPluginsRegistry.featurePlugins().foldLeft(baseUnit) { case (unit, plugin) =>
        plugin.onModelParsed(url, unit)
      }
    }
  }

  private def parseReferences(root: Root, domainPlugin: AMFDocumentPlugin): Future[Root] = {
    val referenceCollector = domainPlugin.referenceCollector()
    val refs               = referenceCollector.traverse(root.parsed, ctx)

    refs.distinct
      .filter(_.isRemote)
      .foreach(link => {
        references += link
          .resolve(remote, Some(context), None, domainPlugin.ID, cache, ctx)
          .map(ParsedReference(_, link))
      })

    Future.sequence(references).map(rs => root.copy(references = rs, vendor = domainPlugin.ID))
  }

  private def resolve(): Future[Content] = remote.resolve(location, base)

  def root(): Future[Root] = resolve().map(parseSyntax).flatMap {
    case Right(document: Root) =>
      AMFPluginsRegistry.documentPluginForMediaType(document.mediatype).find(_.canParse(document)) match {
        case Some(domainPlugin) =>
          parseReferences(document, domainPlugin)
        case None =>
          Future {
            document
          }
      }
    case Left(content) =>
      throw new Exception(s"Cannot parse document with mime type ${content.mime.getOrElse("none")}")
  }
}

object AMFCompiler {
  def init() {
    // We register ourselves as the Runtime compiler
    if (RuntimeCompiler.compiler.isEmpty) {
      RuntimeCompiler.register(
        (url: String,
         remote: Platform,
         base: Option[Context],
         mediaType: Option[String],
         vendor: String,
         referenceKind: ReferenceKind,
         cache: Cache,
         ctx: Option[ParserContext]) => {
          new AMFCompiler(url, remote, base, mediaType, vendor, referenceKind, cache, ctx).build()
        })
    }
  }
}

case class Root(parsed: ParsedDocument,
                location: String,
                mediatype: String,
                references: Seq[ParsedReference],
                referenceKind: ReferenceKind,
                vendor: String,
                raw: String)
