package amf.mcp.internal.plugins.validation

import amf.core.client.common.validation.{MCPSchemaProfile, ProfileName}
import amf.core.client.common.{HighPriority, PluginPriority}
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.validation.AMFValidationReport
import amf.core.internal.plugins.validation.{ValidationInfo, ValidationOptions, ValidationResult}
import amf.core.internal.validation.ValidationConfiguration
import amf.shapes.client.scala.model.document.JsonLDInstanceDocument
import amf.shapes.internal.validation.plugin.BaseModelValidationPlugin

import scala.concurrent.{ExecutionContext, Future}

class MCPValidationPlugin extends BaseModelValidationPlugin {

  override val id: String = this.getClass.getSimpleName

  override def priority: PluginPriority = HighPriority

  override def applies(info: ValidationInfo): Boolean = info.baseUnit.isInstanceOf[JsonLDInstanceDocument]

  override def validate(unit: BaseUnit, options: ValidationOptions)(implicit
      executionContext: ExecutionContext
  ): Future[ValidationResult] = {
    val report = MCPValidationHelper.validateMCPInstance(unit.asInstanceOf[JsonLDInstanceDocument])
    Future.successful(ValidationResult(unit, report))
  }

  override protected def profile: ProfileName = MCPSchemaProfile

  override protected def specificValidate(unit: BaseUnit, options: ValidationOptions)(implicit
      executionContext: ExecutionContext
  ): Future[AMFValidationReport] = Future.successful(emptyReport(unit.location()))

  override protected def withResolvedModel[T](unit: BaseUnit, profile: ProfileName, conf: ValidationConfiguration)(
      withResolved: (BaseUnit, Option[AMFValidationReport]) => T
  ): T = withResolved(unit, None)

  private def emptyReport(location: Option[String]) = AMFValidationReport(location.getOrElse(""), profile, Nil)
}

object MCPValidationPlugin {
  def apply(): MCPValidationPlugin = new MCPValidationPlugin()
}
