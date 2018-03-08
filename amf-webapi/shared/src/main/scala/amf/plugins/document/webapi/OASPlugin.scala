package amf.plugins.document.webapi

import amf.ProfileNames
import amf.ProfileNames.OAS
import amf.core.Root
import amf.core.client.GenerationOptions
import amf.core.model.document._
import amf.core.model.domain.DomainElement
import amf.core.parser.{LibraryReference, LinkReference, ParserContext}
import amf.core.remote.Platform
import amf.plugins.document.webapi.contexts._
import amf.plugins.document.webapi.model.{Extension, Overlay}
import amf.plugins.document.webapi.parser.OasHeader
import amf.plugins.document.webapi.parser.OasHeader.{Oas20Extension, Oas20Header, Oas20Overlay, Oas30Header}
import amf.plugins.document.webapi.parser.spec.oas._
import amf.plugins.document.webapi.resolution.pipelines.OasResolutionPipeline
import amf.plugins.domain.webapi.models.WebApi
import org.yaml.model.{YDocument, YNode}

trait OASPlugin extends BaseWebApiPlugin {
  override val ID: String = ("OAS " + version).trim

  override val vendors = Seq(ID, "OAS")

  override def specContext: OasSpecEmitterContext

  override def parse(document: Root, parentContext: ParserContext, platform: Platform): Option[BaseUnit] = {
    implicit val ctx: OasWebApiContext =
      new OasWebApiContext(parentContext)
    document.referenceKind match {
      case LibraryReference => Some(OasModuleParser(document).parseModule())
      case LinkReference    => Some(OasFragmentParser(document).parseFragment())
      case _                => detectOasUnit(document)
    }
  }

  private def detectOasUnit(root: Root)(implicit ctx: OasWebApiContext): Option[BaseUnit] = {
    OasHeader(root) map {
      case Oas20Overlay   => OasDocumentParser(root).parseOverlay()
      case Oas20Extension => OasDocumentParser(root).parseExtension()
      case Oas20Header    => OasDocumentParser(root).parseDocument()
      case Oas30Header    => Document()
      case f              => OasFragmentParser(root, Some(f)).parseFragment()
    }
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
}

object OAS20Plugin extends OASPlugin {

  override def specContext: OasSpecEmitterContext = new Oas2SpecEmitterContext()

  override def version: String = "2.0"

  override val validationProfile: String = OAS

  /**
    * Decides if this plugin can parse the provided document instance.
    * this can be used by multiple plugins supporting the same media-type
    * to decide which one will parse the document base on information fromt
    * the document structure
    */
  override def canParse(root: Root): Boolean = OasHeader(root).exists(_ != Oas30Header)

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
    case module: Module             => Some(OasModuleEmitter(module)(specContext).emitModule())
    case document: Document         => Some(OasDocumentEmitter(document)(specContext).emitDocument())
    case external: ExternalFragment => Some(YDocument(YNode(external.encodes.raw.value())))
    case fragment: Fragment         => Some(new OasFragmentEmitter(fragment)(specContext).emitFragment())
    case _                          => None
  }

  /**
    * Resolves the provided base unit model, according to the semantics of the domain of the document
    */
  override def resolve(unit: BaseUnit): BaseUnit = new OasResolutionPipeline().resolve(unit)
}

object OAS30Plugin extends OASPlugin {

  override def specContext: Oas3SpecEmitterContext = new Oas3SpecEmitterContext()

  override def version: String = "3.0.0"

  override val validationProfile: String = ProfileNames.OAS3

  /**
    * Decides if this plugin can parse the provided document instance.
    * this can be used by multiple plugins supporting the same media-type
    * to decide which one will parse the document base on information fromt
    * the document structure
    */
  override def canParse(root: Root): Boolean = OasHeader(root).contains(Oas30Header)

  override def canUnparse(unit: BaseUnit): Boolean = false

  override def unparse(unit: BaseUnit, options: GenerationOptions): Option[YDocument] = unit match {
    case module: Module             => Some(OasModuleEmitter(module)(specContext).emitModule())
    case document: Document         => Some(OasDocumentEmitter(document)(specContext).emitDocument())
    case external: ExternalFragment => Some(YDocument(YNode(external.encodes.raw.value())))
    case fragment: Fragment         => Some(new OasFragmentEmitter(fragment)(specContext).emitFragment())
    case _                          => None
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
