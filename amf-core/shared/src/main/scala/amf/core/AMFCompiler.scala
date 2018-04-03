package amf.core

import amf.core
import amf.core.benchmark.ExecutionLog
import amf.core.exception.CyclicReferenceException
import amf.core.model.document.{BaseUnit, ExternalFragment}
import amf.core.model.domain.ExternalDomainElement
import amf.core.parser.{ParsedDocument, ParsedReference, ParserContext, ReferenceKind, UnspecifiedReference}
import amf.core.plugins.AMFDocumentPlugin
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote._
import amf.core.services.RuntimeCompiler

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.failed

object AMFCompilerRunCount {
  var count = 0
  def nextRun(): Int = synchronized {
    count += 1
    count
  }
}

class AMFCompiler(val rawUrl: String,
                  val remote: Platform,
                  val base: Option[Context],
                  val mediaType: Option[String],
                  val vendor: String,
                  val referenceKind: ReferenceKind = UnspecifiedReference,
                  private val cache: Cache = Cache(),
                  private val baseContext: Option[ParserContext] = None) {

  val url: String                   = new java.net.URI(escapeFileSystemPath(rawUrl)).normalize().toString
  private lazy val context: Context = base.map(_.update(url)).getOrElse(core.remote.Context(remote, url))
  private lazy val location         = context.current
  private val ctx: ParserContext =
    baseContext.getOrElse(ParserContext(url))

  def build(): Future[BaseUnit] = {
    ExecutionLog.log(s"AMFCompiler#build: Building $rawUrl")
    if (context.hasCycles) failed(new CyclicReferenceException(context.history))
    else
      cache.getOrUpdate(location) { () =>
        ExecutionLog.log(s"AMFCompiler#build: compiling $rawUrl")
        compile()
      }
  }

  private def compile() = resolve().map(parseSyntax).flatMap(parseDomain)

  private def parseSyntax(inputContent: Content): Either[Content, Root] = {
    ExecutionLog.log(s"AMFCompiler#parseSyntax: parsing syntax $rawUrl")
    val content = AMFPluginsRegistry.featurePlugins().foldLeft(inputContent) {
      case (input, plugin) =>
        plugin.onBeginDocumentParsing(url, input, referenceKind, vendor)
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
        val document = AMFPluginsRegistry.featurePlugins().foldLeft(inputDocument) {
          case (doc, plugin) =>
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
    val result = ExternalDomainElement().withId(content.url + "#/").withRaw(content.stream.toString) //
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
    ExecutionLog.log(s"AMFCompiler#parseDomain: parsing syntax $rawUrl")
    val currentRun = ctx.parserCount
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
                .withId(document.location)
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
      // we setup the run for the parsed unit
      baseUnit.parserRun = Some(currentRun)
      ExecutionLog.log(s"AMFCompiler#parseDomain: model ready $rawUrl")
      AMFPluginsRegistry.featurePlugins().foldLeft(baseUnit) {
        case (unit, plugin) =>
          plugin.onModelParsed(url, unit)
      }
    }
  }

  private def parseReferences(root: Root, domainPlugin: AMFDocumentPlugin): Future[Root] = {
    val handler = domainPlugin.referenceHandler()
    val refs    = handler.collect(root.parsed, ctx)

    val units = refs.toReferences
      .filter(_.isRemote)
      .map(link => {
        link
          .resolve(context, None, domainPlugin.ID, cache, ctx)
          .flatMap(u => {
            val reference = ParsedReference(u, link)
            handler.update(reference, ctx, context).map(Some(_))
          })
          .recover {
            case e: FileNotFound =>
              link.refs.map(_.node).foreach { ref =>
                ctx.violation(link.url, e.getMessage, ref)
              }
              None
          }
      })
    Future.sequence(units).map(rs => root.copy(references = rs.flatten, vendor = domainPlugin.ID))
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

  protected def escapeFileSystemPath(rawUrl: String): String = rawUrl.replace(" ", "%20")
}

object AMFCompiler {
  def init() {
    // We register ourselves as the Runtime compiler
    if (RuntimeCompiler.compiler.isEmpty) {
      RuntimeCompiler.register(
        (url: String,
         base: Context,
         mediaType: Option[String],
         vendor: String,
         referenceKind: ReferenceKind,
         cache: Cache,
         ctx: Option[ParserContext]) => {
          new AMFCompiler(url, base.platform, Some(base), mediaType, vendor, referenceKind, cache, ctx).build()
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
