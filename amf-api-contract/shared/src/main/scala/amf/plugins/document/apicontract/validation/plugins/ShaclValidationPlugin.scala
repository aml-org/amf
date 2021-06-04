package amf.plugins.document.apicontract.validation.plugins

import amf.ProfileName
import amf.client.remod.amfcore.plugins.validate.ValidationOptions
import amf.core.benchmark.ExecutionLog.log
import amf.core.model.document.BaseUnit
import amf.core.services.ShaclValidationOptions
import amf.core.validation.{AMFValidationReport, EffectiveValidations, ShaclReportAdaptation}
import amf.plugins.features.validation.shacl.ShaclValidator

import scala.concurrent.{ExecutionContext, Future}

trait ShaclValidationPlugin extends BaseApiValidationPlugin with ShaclReportAdaptation {

  override protected def specificValidate(unit: BaseUnit, profile: ProfileName, options: ValidationOptions)(
      implicit executionContext: ExecutionContext): Future[AMFValidationReport] = {
    validateWithShacl(unit, profile, options.effectiveValidations)
  }

  protected def validator(options: ShaclValidationOptions): ShaclValidator

  private def validateWithShacl(unit: BaseUnit, profile: ProfileName, validations: EffectiveValidations)(
      implicit executionContext: ExecutionContext): Future[AMFValidationReport] = {

    val shaclOptions = DefaultShaclOptions().withMessageStyle(profile.messageStyle)

    log("WebApiValidations#validationRequestsForBaseUnit: validating now WebAPI")

    validator(shaclOptions)
      .validate(unit, validations.effective.values.toSeq)
      .map { report =>
        adaptToAmfReport(unit, profile, report, validations)
      }
  }
}
