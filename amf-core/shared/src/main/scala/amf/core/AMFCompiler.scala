package amf.core

import amf.core
import amf.core.exception.CyclicReferenceException
import amf.core.model.document.BaseUnit
import amf.core.model.domain.idCounter
import amf.core.parser.{ParsedDocument, ParsedReference, ParserContext, ReferenceKind}
import amf.core.plugins.AMFDocumentPlugin
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote._
import amf.core.services.RuntimeCompiler

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.failed


class AMFCompiler(val url: String,
                  val remote: Platform,
                  val base: Option[Context],
                  val mediaType: String,
                  val vendor: String,
                  val referenceKind: ReferenceKind,
                  private val cache: Cache,
                  private val baseContext: Option[ParserContext] = None) {

  private lazy val context: Context                           = base.map(_.update(url)).getOrElse(core.remote.Context(remote, url))
  private lazy val location                                   = context.current
  private val references: ListBuffer[Future[ParsedReference]] = ListBuffer()
  private val ctx: ParserContext = baseContext.getOrElse(ParserContext(url, Seq.empty))


  def build(): Future[BaseUnit] = {
    // Reset the data node counter
    idCounter.reset()

    if (context.hasCycles) failed(new CyclicReferenceException(context.history))
    else
      cache.getOrUpdate(location) { () =>
        compile()
      }
  }

  private def compile() = {
    resolve() map  { content: Content =>
      parseSyntax(content)
    } flatMap { root: Root =>
      parseDomain(root)
    }
  }

  private def parseSyntax(content: Content) = {
    var effectiveMediaType = content.mime.getOrElse(mediaType)
    val parsed: Option[ParsedDocument] = AMFPluginsRegistry.syntaxPluginForMediaType(effectiveMediaType) match {
      case Some(syntaxPlugin) => syntaxPlugin.parse(effectiveMediaType, content.stream)
      case None if effectiveMediaType != mediaType =>
        effectiveMediaType = mediaType
        AMFPluginsRegistry.syntaxPluginForMediaType(mediaType) match {
          case Some(syntaxPlugin) => syntaxPlugin.parse(effectiveMediaType, content.stream)
          case None               => None
        }
      case _ => None
    }
    parsed match {
      case Some(doc) => Root(doc, content.url, effectiveMediaType, Seq(), referenceKind, vendor, content.stream.toString)
      case _         => throw new Exception(s"Unsupported media type $mediaType, ${content.mime.getOrElse("")}")
    }
  }

  private def parseDomain(document: Root): Future[BaseUnit] = {
    val domainPluginOption = AMFPluginsRegistry.documentPluginForVendor(vendor).find { plugin =>
      plugin.canParse(document) // && plugin.domainSyntaxes.contains(document.mediatype)
    } match {
      case Some(domainPlugin) => Some(domainPlugin)
      case None => AMFPluginsRegistry.documentPluginForMediaType(document.mediatype).find(_.canParse(document))
    }

    domainPluginOption match {
      case Some(domainPlugin) =>
        parseReferences(document, domainPlugin) map { documentWithReferences =>
          domainPlugin.parse(documentWithReferences, ctx, remote) match {
            case Some(baseUnit) => baseUnit
            case None           => throw new Exception(s"Cannot parse domain model for media type ${document.mediatype} with plugin ${domainPlugin.ID} $domainPlugin")
          }
        }
      case None => throw new Exception(s"Cannot parse domain model for media type ${document.mediatype}")
    }
  }

  private def parseReferences(root: Root, domainPlugin: AMFDocumentPlugin): Future[Root] = {
    val referenceCollector = domainPlugin.referenceCollector()
    val refs = referenceCollector.traverse(root.parsed, ctx)

    refs
      .filter(_.isRemote)
      .foreach(link => {
        references += link
          .resolve(remote, Some(context), root.mediatype, domainPlugin.ID, cache, ctx)
          .map(r => ParsedReference(r, link.url, link.kind))
      })

    Future.sequence(references).map(rs => { root.copy(references = rs, vendor = domainPlugin.ID) })
  }

  private def resolve(): Future[Content] = remote.resolve(location, base)

  def root(): Future[Root] = resolve().map(parseSyntax).flatMap { document: Root =>
    AMFPluginsRegistry.documentPluginForMediaType(document.mediatype).find(_.canParse(document)) match {
      case Some(domainPlugin) =>
        parseReferences(document, domainPlugin)
      case None => Future { document }
    }
  }
}

object AMFCompiler {
  def init() {
    // We register ourselves as the Runtime compiler
    if (RuntimeCompiler.compiler.isEmpty) {
      RuntimeCompiler.register(new RuntimeCompiler {
        override def build(url: String, remote: Platform, base: Option[Context], mediaType: String, vendor: String, referenceKind: ReferenceKind, cache: Cache, ctx: Option[ParserContext]): Future[BaseUnit] = {
          new AMFCompiler(url, remote, base, mediaType, vendor, referenceKind, cache, ctx).build()
        }
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
