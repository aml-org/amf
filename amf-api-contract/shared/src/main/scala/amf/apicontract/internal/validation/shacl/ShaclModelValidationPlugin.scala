package amf.apicontract.internal.validation.shacl

import amf.aml.internal.validate.SemanticExtensionConstraints
import amf.apicontract.internal.validation.plugin.BaseApiValidationPlugin
import amf.core.client.common.validation.ProfileName
import amf.core.client.common.{HighPriority, PluginPriority}
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.validation.AMFValidationReport
import amf.core.internal.plugins.validation.ValidationOptions
import amf.core.internal.validation.ShaclReportAdaptation
import amf.validation.internal.shacl.custom.CustomShaclValidator
import amf.validation.internal.shacl.custom.CustomShaclValidator.CustomShaclFunctions

import scala.concurrent.{ExecutionContext, Future}

case class ShaclModelValidationPlugin(profile: ProfileName)
    extends BaseApiValidationPlugin
    with ShaclReportAdaptation
    with SemanticExtensionConstraints {

  override val id: String = this.getClass.getSimpleName

  override def priority: PluginPriority = HighPriority

  override protected def specificValidate(unit: BaseUnit, options: ValidationOptions)(
      implicit executionContext: ExecutionContext): Future[AMFValidationReport] = {
    validateWithShacl(unit, options: ValidationOptions)
  }

  private def validateWithShacl(unit: BaseUnit, options: ValidationOptions)(
      implicit executionContext: ExecutionContext): Future[AMFValidationReport] = {

    val validator = new CustomShaclValidator(functions, profile.messageStyle)
    val validations =
      withSemanticExtensionsConstraints(effectiveOrException(options.config, profile), options.config.constraints)

    validator
      .validate(unit, validations.effective.values.toSeq)
      .map { report =>
        adaptToAmfReport(unit, profile, report, validations)
      }
  }

  private val functions: CustomShaclFunctions = CustomShaclFunctions.functions
}
