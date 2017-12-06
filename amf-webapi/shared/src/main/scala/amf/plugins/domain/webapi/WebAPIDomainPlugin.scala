package amf.plugins.domain.webapi

import amf.core.metamodel.domain.extensions.{CustomDomainPropertyModel, DomainExtensionModel}
import amf.core.plugins.{AMFDomainPlugin, AMFPlugin}
import amf.plugins.domain.shapes.DataShapesDomainPlugin
import amf.plugins.domain.shapes.metamodel.CreativeWorkModel
import amf.plugins.domain.webapi.annotations.ParentEndPoint
import amf.plugins.domain.webapi.metamodel._
import amf.plugins.domain.webapi.metamodel.security._
import amf.plugins.domain.webapi.metamodel.templates.{
  ParametrizedResourceTypeModel,
  ParametrizedTraitModel,
  ResourceTypeModel,
  TraitModel
}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object WebAPIDomainPlugin extends AMFDomainPlugin {

  override val ID = "WebAPI Domain"

  override def dependencies() = Seq(DataShapesDomainPlugin)

  override def modelEntities = Seq(
    WebApiModel,
    CreativeWorkModel,
    OrganizationModel,
    LicenseModel,
    EndPointModel,
    OperationModel,
    ParameterModel,
    PayloadModel,
    RequestModel,
    ResponseModel,
    CustomDomainPropertyModel,
    DomainExtensionModel,
    ParametrizedSecuritySchemeModel,
    ScopeModel,
    SecuritySchemeModel,
    SettingsModel,
    OAuth1SettingsModel,
    OAuth2SettingsModel,
    ApiKeySettingsModel,
    TraitModel,
    ResourceTypeModel,
    ParametrizedResourceTypeModel,
    ParametrizedTraitModel
  )

  override def serializableAnnotations() = Map(
    "parent-end-point" -> ParentEndPoint
  )

  override def init(): Future[AMFPlugin] = Future { this }
}
