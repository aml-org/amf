package amf.plugins.document.webapi

import amf.AmfProfile
import amf.client.plugins.{AMFDocumentPlugin, AMFDomainPlugin, AMFPlugin}
import amf.core.Root
import amf.core.client.ParsingOptions
import amf.core.emitter.{RenderOptions, ShapeRenderOptions}
import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.{BaseUnit, PayloadFragment}
import amf.core.parser.{ParserContext, SimpleReferenceHandler, SyamlParsedDocument}
import amf.core.remote.{Payload, Platform}
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.plugins.document.webapi.contexts.parser.raml.PayloadContext
import amf.plugins.document.webapi.parser.PayloadParser
import amf.plugins.document.webapi.parser.spec.common.PayloadEmitter
import amf.plugins.document.webapi.resolution.pipelines.ValidationResolutionPipeline
import amf.plugins.domain.shapes.DataShapesDomainPlugin
import amf.plugins.domain.webapi.WebAPIDomainPlugin
import org.yaml.builder.{DocBuilder, YDocumentBuilder}
import org.yaml.model.{YDocument, YMap, YScalar}

import scala.concurrent.{ExecutionContext, Future}

object PayloadPlugin extends AMFDocumentPlugin {

  override val ID: String = Payload.name

  val vendors: Seq[String] = Seq(Payload.name)

  override def modelEntities: Nil.type = Nil

  override def serializableAnnotations(): Map[String, Nothing] = Map.empty

  override def dependencies(): Seq[AMFDomainPlugin] = Seq(WebAPIDomainPlugin, DataShapesDomainPlugin)

  // we are looking for documents with a very specific payload
  // otherwise, this plugin can become the fallback option.
  // Fallback option should be an external fragment.
  override def documentSyntaxes: Seq[String] = Seq(
    "application/amf+json",
    "application/amf+yaml",
    "application/payload+json",
    "application/payload+yaml"
  )

  override def parse(root: Root,
                     parentContext: ParserContext,
                     platform: Platform,
                     options: ParsingOptions): Option[PayloadFragment] = {
    root.parsed match {
      case parsed: SyamlParsedDocument =>
        implicit val ctx: PayloadContext =
          new PayloadContext(root.location, parentContext.refs, parentContext, options = options)
        Some(PayloadParser(parsed.document, root.location, root.mediatype).parseUnit())
      case _ =>
        None
    }
  }

  override def canParse(root: Root): Boolean                                   = notRAML(root) && notOAS(root) // any document can be parsed as a Payload
  override def referenceHandler(eh: ErrorHandler): SimpleReferenceHandler.type = SimpleReferenceHandler

  private def notRAML(root: Root) = root.parsed match {
    case parsed: SyamlParsedDocument => parsed.comment.isEmpty || !parsed.comment.exists(_.startsWith("%"))
    case _                           => false
  }

  private def notOAS(root: Root) = root.parsed match {
    case parsed: SyamlParsedDocument =>
      parsed.document.node.value match {
        case map: YMap =>
          !map.entries.exists(_.key.value.asInstanceOf[YScalar].text.startsWith("swagger"))
        case _ => true
      }
    case _ =>
      false
  }

  /* Unparsing payloads not supported */
  override def emit[T](unit: BaseUnit,
                       builder: DocBuilder[T],
                       renderOptions: RenderOptions,
                       shapeRenderOptions: ShapeRenderOptions = ShapeRenderOptions()): Boolean =
    (builder, unit) match {
      case (sb: YDocumentBuilder, p: PayloadFragment) =>
        sb.document = PayloadEmitter(p.encodes)(renderOptions.errorHandler).emitDocument()
        true
      case _ => false
    }

  override protected def unparseAsYDocument(
      unit: BaseUnit,
      renderOptions: RenderOptions,
      shapeRenderOptions: ShapeRenderOptions = ShapeRenderOptions()): Option[YDocument] =
    unit match {
      case p: PayloadFragment =>
        Some(PayloadEmitter(p.encodes)(renderOptions.errorHandler).emitDocument())
      case _ => None
    }

  override def canUnparse(unit: BaseUnit): Boolean = unit.isInstanceOf[PayloadFragment]

  /**
    * Resolves the provided base unit model, according to the semantics of the domain of the document
    */
  override def resolve(unit: BaseUnit,
                       errorHandler: ErrorHandler,
                       pipelineId: String = ResolutionPipeline.DEFAULT_PIPELINE): BaseUnit =
    new ValidationResolutionPipeline(AmfProfile, errorHandler).resolve(unit)

  override def init()(implicit executionContext: ExecutionContext): Future[AMFPlugin] = {
    Future { this }
  }

  /**
    * Does references in this type of documents be recursive?
    */
  override val allowRecursiveReferences: Boolean = false
}
