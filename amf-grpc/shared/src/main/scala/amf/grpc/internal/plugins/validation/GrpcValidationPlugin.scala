package amf.grpc.internal.plugins.validation

import amf.core.client.common.validation.{GrpcProfile, ProfileName}
import amf.core.client.common.{HighPriority, PluginPriority}
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.validation.AMFValidationReport
import amf.core.internal.plugins.validation.{ValidationInfo, ValidationOptions}
import amf.core.internal.validation.ValidationConfiguration
import amf.shapes.client.scala.model.document.JsonLDInstanceDocument
import amf.shapes.internal.validation.plugin.BaseModelValidationPlugin

import scala.concurrent.{ExecutionContext, Future}

class GrpcValidationPlugin extends BaseModelValidationPlugin {

  override val id: String = this.getClass.getSimpleName

  override def priority: PluginPriority = HighPriority

  override def applies(info: ValidationInfo): Boolean = info.baseUnit.isInstanceOf[JsonLDInstanceDocument]

  override protected def profile: ProfileName = GrpcProfile

  override protected def specificValidate(unit: BaseUnit, options: ValidationOptions)(implicit
      executionContext: ExecutionContext
  ): Future[AMFValidationReport] = {
    // TODO here will be the call to the external library to validate the proto3 file
    Future.successful(emptyReport(unit.location()))
  }

  override protected def withResolvedModel[T](unit: BaseUnit, profile: ProfileName, conf: ValidationConfiguration)(
      withResolved: (BaseUnit, Option[AMFValidationReport]) => T
  ): T = withResolved(unit, None)

  private def emptyReport(location: Option[String]) = AMFValidationReport(location.getOrElse(""), profile, Nil)
}

object GrpcValidationPlugin {
  def apply(): GrpcValidationPlugin = new GrpcValidationPlugin()
}
