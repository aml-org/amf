package amf.shapes.internal.validation.shacl

import amf.aml.internal.validate.SemanticExtensionConstraints
import amf.core.client.common.validation.ProfileName
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.validation.AMFValidationReport
import amf.core.internal.plugins.validation.ValidationOptions
import amf.core.internal.validation.ShaclReportAdaptation
import amf.shapes.internal.validation.plugin.BaseModelValidationPlugin
import amf.validation.internal.shacl.custom.CustomShaclValidator
import amf.validation.internal.shacl.custom.CustomShaclValidator.CustomShaclFunctions

import scala.concurrent.ExecutionContext

trait BaseShaclModelValidationPlugin
    extends ShaclReportAdaptation
    with SemanticExtensionConstraints
    with BaseModelValidationPlugin {

  val profile: ProfileName

  protected def validateWithShacl(unit: BaseUnit, options: ValidationOptions)(implicit
      executionContext: ExecutionContext
  ): AMFValidationReport = {

    val validator = new CustomShaclValidator(functions, profile.messageStyle)
    val validations =
      withSemanticExtensionsConstraints(effectiveOrException(options.config, profile), options.config.constraints)

    val report = validator.validate(unit, validations.effective.values.toSeq)
    adaptToAmfReport(unit, profile, report, validations)
  }

  protected val functions: CustomShaclFunctions

}
