package amf.apicontract.internal.validation.shacl

import amf.apicontract.internal.validation.plugin.BaseApiValidationPlugin
import amf.core.client.common.validation.ProfileName
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.validation.AMFValidationReport
import amf.core.internal.plugins.validation.{ValidationInfo, ValidationOptions}
import amf.core.internal.validation.ShaclReportAdaptation
import amf.validation.internal.shacl.custom.CustomShaclValidator
import amf.validation.internal.shacl.custom.CustomShaclValidator.CustomShaclFunctions

import scala.concurrent.{ExecutionContext, Future}

object ShaclModelValidationPlugin extends BaseApiValidationPlugin with ShaclReportAdaptation {

  override val id: String = this.getClass.getSimpleName

  override protected def specificValidate(unit: BaseUnit, profile: ProfileName, options: ValidationOptions)(
      implicit executionContext: ExecutionContext): Future[AMFValidationReport] = {
    validateWithShacl(unit, profile, options)
  }

  private def validateWithShacl(unit: BaseUnit, profile: ProfileName, options: ValidationOptions)(
      implicit executionContext: ExecutionContext): Future[AMFValidationReport] = {

    val validator = new CustomShaclValidator(functions, profile.messageStyle)

    validator
      .validate(unit, options.effectiveValidations.effective.values.toSeq)
      .map { report =>
        adaptToAmfReport(unit, profile, report, options.effectiveValidations)
      }
  }

  private val functions: CustomShaclFunctions = CustomShaclFunctions.functions
}
