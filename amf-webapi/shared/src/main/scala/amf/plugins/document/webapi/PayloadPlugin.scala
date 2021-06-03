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
import amf.plugins.parse.PayloadParsePlugin
import org.yaml.builder.{DocBuilder, YDocumentBuilder}
import org.yaml.model.{YDocument, YMap, YScalar}

import scala.concurrent.{ExecutionContext, Future}

object PayloadPlugin extends AMFDocumentPlugin {

  override val ID: String = Payload.name

  val vendors: Seq[String] = Seq(Payload.mediaType, "application/payload+json", "application/payload+yaml")

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

  override def parse(root: Root, ctx: ParserContext): PayloadFragment = PayloadParsePlugin.parse(root, ctx)

  override def canParse(root: Root): Boolean                                      = PayloadParsePlugin.applies(root)
  override def referenceHandler(eh: AMFErrorHandler): SimpleReferenceHandler.type = SimpleReferenceHandler

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
