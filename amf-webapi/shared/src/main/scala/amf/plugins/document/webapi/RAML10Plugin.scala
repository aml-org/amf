package amf.plugins.document.webapi

import amf.ProfileNames
import amf.core.Root
import amf.core.client.GenerationOptions
import amf.core.model.document._
import amf.core.model.domain.DomainElement
import amf.core.parser.{EmptyFutureDeclarations, ParserContext}
import amf.core.plugins.{AMFDocumentPlugin, AMFValidationPlugin}
import amf.core.remote.Platform
import amf.core.validation.{AMFValidationReport, EffectiveValidations}
import amf.plugins.document.webapi.contexts.{RamlSpecAwareContext, WebApiContext}
import amf.plugins.document.webapi.model._
import amf.plugins.document.webapi.parser.spec.raml.{RamlDocumentEmitter, RamlFragmentEmitter, RamlModuleEmitter, _}
import amf.plugins.document.webapi.parser.{RamlFragment, RamlHeader}
import amf.plugins.document.webapi.references.WebApiReferenceCollector
import amf.plugins.document.webapi.resolution.pipelines.RamlResolutionPipeline
import amf.plugins.document.webapi.validation.WebApiValidations
import amf.plugins.domain.shapes.DataShapesDomainPlugin
import amf.plugins.domain.webapi.WebAPIDomainPlugin
import amf.plugins.domain.webapi.models.WebApi

import scala.concurrent.Future

object RAML10Plugin extends AMFDocumentPlugin with AMFValidationPlugin with WebApiValidations with WebApiDocuments {

  val ID: String = "RAML 1.0"

  val vendors = Seq("RAML 1.0", "RAML")

  override def serializableAnnotations() = webApiAnnotations

  override def modelEntities = webApiDocuments

  override def dependencies() = Seq(WebAPIDomainPlugin, DataShapesDomainPlugin)

  def canParse(root: Root): Boolean = RamlHeader(root) match {
    case Some(RamlHeader.Raml10)          => true
    case Some(RamlHeader.Raml10Overlay)   => true
    case Some(RamlHeader.Raml10Extension) => true
    case Some(RamlHeader.Raml10Library)   => true
    case Some(fragment: RamlFragment)     => true
    case _                                => false
  }

  override def parse(root: Root, parentContext: ParserContext, platform: Platform): Option[BaseUnit] = {
    val updated: WebApiContext = new WebApiContext(RamlSyntax, ProfileNames.RAML, RamlSpecAwareContext, parentContext)
    val cleanNested = ParserContext(root.location, root.references, EmptyFutureDeclarations())
    val clean: WebApiContext = new WebApiContext(RamlSyntax, ProfileNames.RAML, RamlSpecAwareContext, cleanNested)

    RamlHeader(root) match {
      case Some(RamlHeader.Raml10)          => Some(RamlDocumentParser(root)(updated).parseDocument())
      case Some(RamlHeader.Raml10Overlay)   => Some(RamlDocumentParser(root)(updated).parseOverlay())
      case Some(RamlHeader.Raml10Extension) => Some(RamlDocumentParser(root)(updated).parseExtension())
      case Some(RamlHeader.Raml10Library)   => Some(RamlModuleParser(root)(clean).parseModule())
      case Some(fragment: RamlFragment)     => RamlFragmentParser(root, fragment)(updated).parseFragment()
      case _                                => None
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
    case _: DocumentationItemFragment         => true
    case _: DataTypeFragment                  => true
    case _: NamedExampleFragment              => true
    case _: ResourceTypeFragment              => true
    case _: TraitFragment                     => true
    case _: AnnotationTypeDeclarationFragment => true
    case _: SecuritySchemeFragment            => true
    case _: ExternalFragment                  => true
    case _                                    => false
  }

  override def unparse(unit: BaseUnit, options: GenerationOptions) = unit match {
    case module: Module     => Some(RamlModuleEmitter(module).emitModule())
    case document: Document => Some(RamlDocumentEmitter(document).emitDocument())
    case fragment: Fragment => Some(new RamlFragmentEmitter(fragment).emitFragment())
    case _                  => None
  }

  override def referenceCollector() = new WebApiReferenceCollector(ID)

  /**
    * List of media types used to encode serialisations of
    * this domain
    */
  override def documentSyntaxes = Seq(
    "application/raml",
    "application/raml+json",
    "application/raml+yaml",
    "text/yaml",
    "text/x-yaml",
    "application/yaml",
    "application/x-yaml",
    "text/vnd.yaml"
  )

  /**
    * Validation profiles supported by this plugin by default
    */
  override def domainValidationProfiles(platform: Platform) = defaultValidationProfiles

  def validationRequest(baseUnit: BaseUnit, profile: String, validations: EffectiveValidations, platform: Platform): Future[AMFValidationReport] =
    validationRequestsForBaseUnit(baseUnit, profile, validations, ProfileNames.RAML, platform)

  /**
    * Resolves the provided base unit model, according to the semantics of the domain of the document
    */
  override def resolve(unit: BaseUnit) = new RamlResolutionPipeline().resolve(unit)

}
