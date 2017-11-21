package amf.core

import amf.compiler.{ParsedDocument, ParsedReference}
import amf.document.BaseUnit
import amf.domain.extensions.idCounter
import amf.exception.CyclicReferenceException
import amf.framework.parser.ReferenceKind
import amf.framework.plugins.AMFDomainPlugin
import amf.plugins.domain.graph.AMFGraphPlugin
import amf.plugins.domain.payload.PayloadPlugin
import amf.plugins.domain.vocabularies.RAMLExtensionsPlugin
import amf.plugins.domain.webapi.{OAS20Plugin, RAML10Plugin}
import amf.plugins.syntax.SYamlSyntaxPlugin
import amf.remote.Syntax.{Json, Yaml}
import amf.remote._
import amf.spec.ParserContext
import amf.validation.Validation

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
                  val currentValidation: Validation,
                  private val cache: Cache,
                  private val baseContext: Option[ParserContext] = None) {

  private lazy val context: Context                           = base.map(_.update(url)).getOrElse(Context(remote, url))
  private lazy val location                                   = context.current
  private val references: ListBuffer[Future[ParsedReference]] = ListBuffer()
  private val ctx: ParserContext = baseContext.getOrElse(ParserContext(currentValidation, url, Seq.empty))

  // temporary
  AMFPluginsRegistry.registerSyntaxPlugin(new SYamlSyntaxPlugin())
  AMFPluginsRegistry.registerDomainPlugin(new AMFGraphPlugin())
  AMFPluginsRegistry.registerDomainPlugin(new PayloadPlugin())
  AMFPluginsRegistry.registerDomainPlugin(new RAMLExtensionsPlugin())
  AMFPluginsRegistry.registerDomainPlugin(new OAS20Plugin())
  AMFPluginsRegistry.registerDomainPlugin(new RAML10Plugin())
  //

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
    val domainPluginOption = AMFPluginsRegistry.domainPluginForVendor(vendor).find { plugin =>
      plugin.canParse(document) && plugin.domainSyntaxes.contains(document.mediatype)
    } match {
      case Some(domainPlugin) => Some(domainPlugin)
      case None => AMFPluginsRegistry.domainPluginForMediaType(document.mediatype).find(_.canParse(document))
    }

    domainPluginOption match {
      case Some(domainPlugin) =>
        parseReferences(document, domainPlugin) map { documentWithReferences =>
          domainPlugin.parse(documentWithReferences, ctx, remote) match {
            case Some(baseUnit) => baseUnit
            case None           => throw new Exception(s"Cannot parse domain model for media type ${document.mediatype} with plugin ${domainPlugin.ID} ${domainPlugin}")
          }
        }
      case None => throw new Exception(s"Cannot parse domain model for media type ${document.mediatype}")
    }
  }

  private def parseReferences(root: Root, domainPlugin: AMFDomainPlugin): Future[Root] = {
    val referenceCollector = domainPlugin.referenceCollector()
    val refs = referenceCollector.traverse(root.parsed, currentValidation, ctx)

    refs
      .filter(_.isRemote)
      .foreach(link => {
        references += link
          .resolve(remote, Some(context), root.mediatype, domainPlugin.ID, ctx.validation, cache, remote.dialectsRegistry, ctx)
          .map(r => ParsedReference(r, link.url, link.kind))
      })

    Future.sequence(references).map(rs => { root.copy(references = rs, vendor = domainPlugin.ID) })
  }

  private def resolve(): Future[Content] = remote.resolve(location, base)

  def root(): Future[Root] = resolve().map(parseSyntax).flatMap { document: Root =>
    AMFPluginsRegistry.domainPluginForMediaType(document.mediatype).find(_.canParse(document)) match {
      case Some(domainPlugin) =>
        parseReferences(document, domainPlugin)
      case None => Future { document }
    }
  }

}

case class Root(parsed: ParsedDocument,
                location: String,
                mediatype: String,
                references: Seq[ParsedReference],
                referenceKind: ReferenceKind,
                vendor: String,
                raw: String) {

  // TODO: remove me, only for compatibility while refactoring
  def oldFormat(): amf.compiler.Root = {
    val mediaType = if (mediatype.indexOf("yaml") > -1 ) {
      Yaml
    } else if (mediatype.indexOf("json") > -1) {
      Json
    } else {
      Unknown
    }

    val hint = vendor match {
      case "RAML 1.0" if mediaType == Yaml    => RamlYamlHint
      case "RAML 1.0" if mediaType == Json    => RamlJsonHint
      case "OAS 2.0" if mediaType == Json     => OasJsonHint
      case "OAS 2.0" if mediaType == Yaml     => OasYamlHint
      case "AMF Payload" if mediaType == Yaml => PayloadYamlHint
      case "AMF Payload" if mediaType == Json => PayloadJsonHint
      case "AMF Extension"                    => ExtensionYamlHint
      case _                                  => AmfJsonHint
    }

    amf.compiler.Root(
      parsed,
      location,
      references,
      referenceKind,
      hint.vendor,
      raw
    )
  }
}
