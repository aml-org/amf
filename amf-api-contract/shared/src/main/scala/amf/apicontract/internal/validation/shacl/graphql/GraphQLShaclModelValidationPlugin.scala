package amf.apicontract.internal.validation.shacl.graphql

import amf.apicontract.internal.validation.plugin.BaseApiValidationPlugin
import amf.apicontract.internal.validation.shacl.APICustomShaclFunctions
import amf.core.client.common.validation.{ProfileName, ProfileNames}
import amf.core.client.common.{HighPriority, PluginPriority}
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.validation.AMFValidationReport
import amf.core.internal.plugins.validation.ValidationOptions
import amf.shapes.internal.validation.shacl.BaseShaclModelValidationPlugin
import amf.validation.internal.shacl.custom.CustomShaclValidator
import amf.validation.internal.shacl.custom.CustomShaclValidator.CustomShaclFunctions

import scala.concurrent.{ExecutionContext, Future}

case class GraphQLShaclModelValidationPlugin(override val profile: ProfileName = ProfileNames.GRAPHQL)
    extends BaseShaclModelValidationPlugin
    with BaseApiValidationPlugin {

  override val id: String = this.getClass.getSimpleName

  override def priority: PluginPriority = HighPriority

  override protected def getValidator: CustomShaclValidator = {
    new CustomShaclValidator(
      functions,
      profile.messageStyle,
      strategy = GraphQLIteratorStrategy
    )
  }

  override protected def specificValidate(unit: BaseUnit, options: ValidationOptions)(implicit
      executionContext: ExecutionContext
  ): Future[AMFValidationReport] = Future(validateWithShacl(unit, options: ValidationOptions))

  override protected val functions: CustomShaclFunctions = APICustomShaclFunctions.functions
}
