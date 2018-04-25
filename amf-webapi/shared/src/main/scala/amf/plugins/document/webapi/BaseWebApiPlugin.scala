package amf.plugins.document.webapi

import amf.core.model.document.BaseUnit
import amf.core.plugins.{AMFDocumentPlugin, AMFPlugin, AMFValidationPlugin}
import amf.core.remote.Platform
import amf.core.unsafe.PlatformSecrets
import amf.core.validation.core.ValidationProfile
import amf.core.validation.{AMFValidationReport, EffectiveValidations}
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

  def version: String

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
    "parsed-json-schema"   -> ParsedJSONSchema,
    "declared-element"     -> DeclaredElement,
    "local-link-path"      -> LocalLinkPath,
    "extension-provenance" -> ExtensionProvenance,
    "form-body-parameter"  -> FormBodyParameter
  )

  val validationProfile: String

  /**
    * Validation profiles supported by this plugin by default
    */
  override def domainValidationProfiles(platform: Platform): Map[String, () => ValidationProfile] =
    defaultValidationProfiles

  def validationRequest(baseUnit: BaseUnit,
                        profile: String,
                        validations: EffectiveValidations,
                        platform: Platform): Future[AMFValidationReport] = {
    validationRequestsForBaseUnit(baseUnit, profile, validations, validationProfile, platform)
  }

  override def init(): Future[AMFPlugin] = Future.successful(this)

}
