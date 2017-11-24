package amf.plugins.domain.webapi

import amf.framework.plugins.AMFDomainPlugin
import amf.plugins.domain.shapes.DataShapesDomainPlugin
import amf.plugins.domain.webapi.metamodel._
import amf.plugins.domain.webapi.metamodel.extensions.DomainExtensionModel
import amf.plugins.domain.webapi.metamodel.security._
import amf.plugins.domain.webapi.metamodel.templates.{ParametrizedResourceTypeModel, ParametrizedTraitModel, ResourceTypeModel, TraitModel}

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
    ParametrizedTraitModel,
    ExternalDomainElementModel
  )

  override def serializableAnnotations() = Map.empty
}
