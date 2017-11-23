package amf.plugins.domain.webapi

import amf.framework.plugins.AMFDomainPlugin
import amf.plugins.domain.shapes.DataShapesDomainPlugin
import amf.plugins.domain.webapi.metamodel._

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
    ResponseModel
  )

  override def serializableAnnotations() = Map.empty
}
