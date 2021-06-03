package amf.plugins.document.webapi

import amf.ProfileName
import amf.client.plugins._
import amf.client.remod.amfcore.config.RenderOptions
import amf.client.remod.amfcore.plugins.validate.AMFValidatePlugin
import amf.core.errorhandling.AMFErrorHandler
import amf.core.metamodel.Obj
import amf.core.model.domain.AnnotationGraphLoader
import amf.core.remote.Vendor
import amf.core.unsafe.PlatformSecrets
import amf.core.validation.core.ValidationProfile
import amf.plugins.document.webapi.annotations.serializable.WebAPISerializableAnnotations
import amf.plugins.document.webapi.contexts.SpecEmitterContext
import amf.plugins.document.webapi.entities.WebAPIEntities
import amf.plugins.document.webapi.references.ApiReferenceHandler
import amf.plugins.domain.shapes.DataShapesDomainPlugin
import amf.plugins.domain.webapi.APIDomainPlugin

import scala.concurrent.{ExecutionContext, Future}

trait BaseWebApiPlugin extends AMFDocumentPlugin with AMFValidationPlugin with PlatformSecrets {

  protected def vendor: Vendor

  override val ID: String = vendor.name

  override def referenceHandler(eh: AMFErrorHandler) = new ApiReferenceHandler(ID)

  override def dependencies(): Seq[AMFPlugin] = Seq(
    APIDomainPlugin,
    DataShapesDomainPlugin,
    ExternalJsonYamlRefsPlugin
  )

  def validVendorsToReference: Seq[String] = List("application/refs+json")

  def specContext(options: RenderOptions, errorHandler: AMFErrorHandler): SpecEmitterContext

  /**
    * Does references in this type of documents be recursive?
    */
  override val allowRecursiveReferences: Boolean = false

  override def modelEntities: Seq[Obj] = WebAPIEntities.entities.values.toSeq

  override def serializableAnnotations(): Map[String, AnnotationGraphLoader] =
    WebAPISerializableAnnotations.annotations

  val validationProfile: ProfileName

  /**
    * Validation profiles supported by this plugin by default
    */
  override def domainValidationProfiles: Seq[ValidationProfile]

  override protected[amf] def getRemodValidatePlugins(): Seq[AMFValidatePlugin] =
    Seq()

  override def init()(implicit executionContext: ExecutionContext): Future[AMFPlugin] = Future.successful(this)

}
