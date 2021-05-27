package amf.plugins.document.webapi

import amf.client.plugins.{AMFDocumentPlugin, AMFDomainPlugin, AMFPlugin}
import amf.client.remod.amfcore.config.RenderOptions
import amf.core.Root
import amf.core.errorhandling.AMFErrorHandler
import amf.core.exception.UnsupportedParsedDocumentException
import amf.core.model.document.{BaseUnit, PayloadFragment}
import amf.core.parser.{ParserContext, SimpleReferenceHandler, SyamlParsedDocument}
import amf.core.remote.Payload
import amf.plugins.document.webapi.contexts.parser.raml.PayloadContext
import amf.plugins.document.webapi.parser.PayloadParser
import amf.plugins.document.webapi.parser.spec.common.PayloadEmitter
import amf.plugins.domain.shapes.DataShapesDomainPlugin
import amf.plugins.domain.webapi.APIDomainPlugin
import org.yaml.builder.{DocBuilder, YDocumentBuilder}
import org.yaml.model.{YDocument, YMap, YScalar}

import scala.concurrent.{ExecutionContext, Future}

object PayloadPlugin extends AMFDocumentPlugin {

  override val ID: String = Payload.name

  val vendors: Seq[String] = Seq(Payload.name)

  override def modelEntities: Nil.type = Nil

  override def serializableAnnotations(): Map[String, Nothing] = Map.empty

  override def dependencies(): Seq[AMFDomainPlugin] = Seq(APIDomainPlugin, DataShapesDomainPlugin)

  override val validVendorsToReference: Seq[String] = Nil

// we are looking for documents with a very specific payload
  // otherwise, this plugin can become the fallback option.
  // Fallback option should be an external fragment.
  override def documentSyntaxes: Seq[String] = Seq(
    Payload.mediaType,
    "application/amf+json",
    "application/amf+yaml",
    "application/payload+json",
    "application/payload+yaml"
  )

  override def parse(root: Root, ctx: ParserContext): PayloadFragment = {
    root.parsed match {
      case parsed: SyamlParsedDocument =>
        implicit val newCtx: PayloadContext =
          new PayloadContext(root.location, ctx.refs, ctx, options = ctx.parsingOptions)
        PayloadParser(parsed.document, root.location, root.mediatype).parseUnit()
      case _ => throw UnsupportedParsedDocumentException
    }
  }

  override def canParse(root: Root): Boolean =
    notRAML(root) && notOAS(root) && notAsync(root) // any document can be parsed as a Payload
  override def referenceHandler(eh: AMFErrorHandler): SimpleReferenceHandler.type = SimpleReferenceHandler

  private def notRAML(root: Root) = root.parsed match {
    case parsed: SyamlParsedDocument => parsed.comment.isEmpty || !parsed.comment.exists(_.startsWith("%"))
    case _                           => false
  }

  private def notAsync(root: Root) = containsHeader(root, List("asyncapi"))
  private def notOAS(root: Root)   = containsHeader(root, List("swagger", "openapi"))

  private def containsHeader(root: Root, validHeaders: Seq[String]) = root.parsed match {
    case parsed: SyamlParsedDocument =>
      parsed.document.node.value match {
        case map: YMap =>
          !map.entries.exists { entry =>
            val text = entry.key.value.asInstanceOf[YScalar].text
            validHeaders.exists(text.startsWith)
          }
        case _ => true
      }
    case _ =>
      false
  }

  /* Unparsing payloads not supported */
  override def emit[T](unit: BaseUnit,
                       builder: DocBuilder[T],
                       renderOptions: RenderOptions,
                       errorHandler: AMFErrorHandler): Boolean =
    (builder, unit) match {
      case (sb: YDocumentBuilder, p: PayloadFragment) =>
        sb.document = PayloadEmitter(p.encodes)(errorHandler).emitDocument()
        true
      case _ => false
    }

  override protected def unparseAsYDocument(unit: BaseUnit,
                                            renderOptions: RenderOptions,
                                            errorHandler: AMFErrorHandler): Option[YDocument] =
    unit match {
      case p: PayloadFragment =>
        Some(PayloadEmitter(p.encodes)(errorHandler).emitDocument())
      case _ => None
    }

  override def canUnparse(unit: BaseUnit): Boolean = unit.isInstanceOf[PayloadFragment]

  override def init()(implicit executionContext: ExecutionContext): Future[AMFPlugin] = {
    Future { this }
  }

  /**
    * Does references in this type of documents be recursive?
    */
  override val allowRecursiveReferences: Boolean = false
}
