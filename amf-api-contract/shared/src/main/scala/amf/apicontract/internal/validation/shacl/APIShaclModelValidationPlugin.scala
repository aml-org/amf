package amf.apicontract.internal.validation.shacl

import amf.apicontract.internal.validation.plugin.BaseApiValidationPlugin
import amf.core.client.common.validation.ProfileName
import amf.core.client.common.{HighPriority, PluginPriority}
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.validation.AMFValidationReport
import amf.core.internal.plugins.validation.ValidationOptions
import amf.shapes.internal.validation.shacl.BaseShaclModelValidationPlugin
import amf.validation.internal.shacl.custom.CustomShaclValidator.CustomShaclFunctions

import scala.concurrent.{ExecutionContext, Future}

case class APIShaclModelValidationPlugin(override val profile: ProfileName)
    extends BaseShaclModelValidationPlugin
    with BaseApiValidationPlugin {

  override val id: String = this.getClass.getSimpleName

  override def priority: PluginPriority = HighPriority

  override protected def specificValidate(unit: BaseUnit, options: ValidationOptions)(implicit
      executionContext: ExecutionContext
  ): Future[AMFValidationReport] = Future(validateWithShacl(unit, options: ValidationOptions))

  override protected val functions: CustomShaclFunctions = APICustomShaclFunctions.functions
}
