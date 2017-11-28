package amf.plugins.document.webapi

import amf.ProfileNames
import amf.core.Root
import amf.core.client.GenerationOptions
import amf.core.model.document._
import amf.core.model.domain.DomainElement
import amf.core.parser.{LibraryReference, LinkReference, ParserContext}
import amf.core.plugins.{AMFDocumentPlugin, AMFValidationPlugin}
import amf.core.remote.Platform
import amf.core.validation.{AMFValidationReport, EffectiveValidations}
import amf.plugins.document.webapi.contexts.{OasSpecAwareContext, WebApiContext}
import amf.plugins.document.webapi.model.{Extension, Overlay}
import amf.plugins.document.webapi.parser.OasHeader
import amf.plugins.document.webapi.parser.OasHeader.{Oas20Extension, Oas20Header, Oas20Overlay}
import amf.plugins.document.webapi.parser.spec.oas._
import amf.plugins.document.webapi.references.WebApiReferenceCollector
import amf.plugins.document.webapi.resolution.pipelines.OasResolutionPipeline
import amf.plugins.document.webapi.validation.WebApiValidations
import amf.plugins.domain.shapes.DataShapesDomainPlugin
import amf.plugins.domain.webapi.WebAPIDomainPlugin
import amf.plugins.domain.webapi.models.WebApi

import scala.concurrent.Future

object OAS20Plugin extends AMFDocumentPlugin with AMFValidationPlugin with WebApiValidations with WebApiDocuments {

  val ID: String = "OAS 2.0"

  val vendors = Seq("OAS 2.0", "OAS")

  override def modelEntities = webApiDocuments

  override def dependencies() = Seq(WebAPIDomainPlugin, DataShapesDomainPlugin)

  override def serializableAnnotations() = webApiAnnotations

  private def detectOasUnit(root: Root)(implicit ctx: WebApiContext): Option[BaseUnit] = {
    OasHeader(root) match {
      case Some(Oas20Overlay)   => Some(OasDocumentParser(root).parseOverlay())
      case Some(Oas20Extension) => Some(OasDocumentParser(root).parseExtension())
      case Some(Oas20Header)    => Some(OasDocumentParser(root).parseDocument())
      case f if f.isDefined     => Some(OasFragmentParser(root, f).parseFragment())
      case _                    => None
    }
  }

  /**
    * Decides if this plugin can parse the provided document instance.
    * this can be used by multiple plugins supporting the same media-type
    * to decide which one will parse the document base on information fromt
    * the document structure
    */
  override def canParse(root: Root): Boolean = {
    OasHeader(root) match {
      case Some(Oas20Overlay)   => true
      case Some(Oas20Extension) => true
      case Some(Oas20Header)    => true
      case f if f.isDefined     => true
      case _                    => false
    }
  }

  override def parse(document: Root, parentContext: ParserContext, platform: Platform) = {
    implicit val ctx: WebApiContext = new WebApiContext(OasSyntax, ProfileNames.OAS, OasSpecAwareContext, parentContext)
    document.referenceKind match {
      case LibraryReference => Some(OasModuleParser(document).parseModule())
      case LinkReference    => Some(OasFragmentParser(document).parseFragment())
      case _       => detectOasUnit(document)
    }
  }

  override def canUnparse(unit: BaseUnit) = unit match {
    case _: Overlay   => true
    case _: Extension => true
    case document: Document => document.encodes.isInstanceOf[WebApi]
    case module: Module =>
      module.declares exists {
        case _:DomainElement  => true
        case _                => false
      }
    case _: Fragment  => true
    case _            => false
  }

  override def unparse(unit: BaseUnit, options: GenerationOptions) = unit match {
    case module: Module     => Some(OasModuleEmitter(module).emitModule())
    case document: Document => Some(OasDocumentEmitter(document).emitDocument())
    case fragment: Fragment => Some(new OasFragmentEmitter(fragment).emitFragment())
    case _                  => None
  }

  override def referenceCollector() = new WebApiReferenceCollector(ID)

  /**
    * Validation profiles supported by this plugin by default
    */
  override def domainValidationProfiles(platform: Platform) = defaultValidationProfiles

  def validationRequest(baseUnit: BaseUnit, profile: String, validations: EffectiveValidations, platform: Platform): Future[AMFValidationReport] =
    validationRequestsForBaseUnit(baseUnit, profile, validations, ProfileNames.OAS, platform)

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
  override def resolve(unit: BaseUnit) = new OasResolutionPipeline().resolve(unit)
}
