package amf.plugins.document.webapi

import amf.ProfileName
import amf.client.plugins.{AMFDocumentPlugin, AMFPlugin, AMFValidationPlugin}
import amf.core.annotations.{DeclaredElement, ExternalFragmentRef, InlineElement}
import amf.core.model.document.BaseUnit
import amf.core.remote.{Platform, Vendor}
import amf.core.unsafe.PlatformSecrets
import amf.core.validation.core.ValidationProfile
import amf.core.validation.{AMFValidationReport, EffectiveValidations}
import amf.internal.environment.Environment
import amf.plugins.document.webapi.annotations._
import amf.plugins.document.webapi.contexts.SpecEmitterContext
import amf.plugins.document.webapi.metamodel.FragmentsTypesModels._
import amf.plugins.document.webapi.metamodel.{ExtensionModel, OverlayModel}
import amf.plugins.document.webapi.references.WebApiReferenceHandler
import amf.plugins.document.webapi.validation.WebApiValidations
import amf.plugins.domain.shapes.DataShapesDomainPlugin
import amf.plugins.domain.webapi.WebAPIDomainPlugin

import scala.concurrent.Future

trait BaseWebApiPlugin extends AMFDocumentPlugin with AMFValidationPlugin with WebApiValidations with PlatformSecrets {

  protected def vendor: Vendor

  override val ID: String = vendor.name

  override def referenceHandler() = new WebApiReferenceHandler(ID, this)

  override def dependencies() = Seq(WebAPIDomainPlugin, DataShapesDomainPlugin, ExternalJsonRefsPlugin)

  def specContext: SpecEmitterContext

  /**
    * Does references in this type of documents be recursive?
    */
  override val allowRecursiveReferences: Boolean = false

  override def modelEntities = Seq(
    ExtensionModel,
    OverlayModel,
    DocumentationItemFragmentModel,
    DataTypeFragmentModel,
    NamedExampleFragmentModel,
    ResourceTypeFragmentModel,
    TraitFragmentModel,
    AnnotationTypeDeclarationFragmentModel,
    SecuritySchemeFragmentModel
  )

  override def serializableAnnotations() = Map(
    "parsed-json-schema"    -> ParsedJSONSchema,
    "external-fragment-ref" -> ExternalFragmentRef,
    "json-schema-id"        -> JSONSchemaId,
    "declared-element"      -> DeclaredElement,
    "inline-element"        -> InlineElement,
    "local-link-path"       -> LocalLinkPath,
    "extension-provenance"  -> ExtensionProvenance,
    "form-body-parameter"   -> FormBodyParameter
  )

  val validationProfile: ProfileName

  /**
    * Validation profiles supported by this plugin by default
    */
  // todo: compute again each map for each web api vendor plug in (ej raml 10 oas 20 etc). Filter each one by vendor? compute only one time the map? the problme its how to add custom validations.
  override def domainValidationProfiles(platform: Platform): Map[String, () => ValidationProfile] =
    defaultValidationProfiles

  def validationRequest(baseUnit: BaseUnit,
                        profile: ProfileName,
                        validations: EffectiveValidations,
                        platform: Platform,
                        env: Environment): Future[AMFValidationReport] = {
    validationRequestsForBaseUnit(baseUnit, profile, validations, validationProfile.messageStyle, platform, env)
  }

  override def init(): Future[AMFPlugin] = Future.successful(this)

}
