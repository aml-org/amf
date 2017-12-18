package amf.plugins.document.webapi

import amf.ProfileNames.OAS
import amf.core.Root
import amf.core.client.GenerationOptions
import amf.core.model.document._
import amf.core.model.domain.DomainElement
import amf.core.parser.{LibraryReference, LinkReference, ParserContext}
import amf.core.remote.Platform
import amf.plugins.document.webapi.contexts.{OasSpecAwareContext, WebApiContext}
import amf.plugins.document.webapi.model.{Extension, Overlay}
import amf.plugins.document.webapi.parser.OasHeader
import amf.plugins.document.webapi.parser.OasHeader.{Oas20Extension, Oas20Header, Oas20Overlay}
import amf.plugins.document.webapi.parser.spec.oas._
import amf.plugins.document.webapi.resolution.pipelines.OasResolutionPipeline
import amf.plugins.domain.webapi.models.WebApi
import org.yaml.model.{YDocument, YNode}

object OAS20Plugin extends BaseWebApiPlugin {

  override val ID: String = "OAS 2.0"

  override val vendors = Seq("OAS 2.0", "OAS")

  override val validationProfile: String = OAS

  private def detectOasUnit(root: Root)(implicit ctx: WebApiContext): Option[BaseUnit] = {
    OasHeader(root) map {
      case Oas20Overlay   => OasDocumentParser(root).parseOverlay()
      case Oas20Extension => OasDocumentParser(root).parseExtension()
      case Oas20Header    => OasDocumentParser(root).parseDocument()
      case f              => OasFragmentParser(root, Some(f)).parseFragment()
    }
  }

  /**
    * Decides if this plugin can parse the provided document instance.
    * this can be used by multiple plugins supporting the same media-type
    * to decide which one will parse the document base on information fromt
    * the document structure
    */
  override def canParse(root: Root): Boolean = OasHeader(root).isDefined

  override def parse(document: Root, parentContext: ParserContext, platform: Platform): Option[BaseUnit] = {
    implicit val ctx: WebApiContext =
      new WebApiContext(OasSyntax, OAS, OasSpecAwareContext, parentContext)
    document.referenceKind match {
      case LibraryReference => Some(OasModuleParser(document).parseModule())
      case LinkReference    => Some(OasFragmentParser(document).parseFragment())
      case _                => detectOasUnit(document)
    }
  }

  override def canUnparse(unit: BaseUnit): Boolean = unit match {
    case _: Overlay         => true
    case _: Extension       => true
    case document: Document => document.encodes.isInstanceOf[WebApi]
    case module: Module =>
      module.declares exists {
        case _: DomainElement => true
        case _                => false
      }
    case _: Fragment => true
    case _           => false
  }

  override def unparse(unit: BaseUnit, options: GenerationOptions): Option[YDocument] = unit match {
    case module: Module     => Some(OasModuleEmitter(module).emitModule())
    case document: Document => Some(OasDocumentEmitter(document).emitDocument())
    case external: ExternalFragment => Some(YDocument(YNode(external.encodes.raw)))
    case fragment: Fragment => Some(new OasFragmentEmitter(fragment).emitFragment())
    case _                  => None
  }

  /**
    * List of media types used to encode serialisations of
    * this domain
    */
  override def documentSyntaxes = Seq(
    "application/json",
    "application/yaml",
    "application/x-yaml",
    "text/yaml",
    "text/vnd.yaml",
    "text/x-yaml",
    "application/openapi+json",
    "application/swagger+json",
    "application/openapi+yaml",
    "application/swagger+yaml",
    "application/openapi",
    "application/swagger"
  )

  /**
    * Resolves the provided base unit model, according to the semantics of the domain of the document
    */
  override def resolve(unit: BaseUnit): BaseUnit = new OasResolutionPipeline().resolve(unit)
}
