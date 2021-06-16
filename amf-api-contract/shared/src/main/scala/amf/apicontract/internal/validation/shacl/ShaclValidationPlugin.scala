package amf.apicontract.internal.validation.shacl

import amf.apicontract.internal.validation.plugin.BaseApiValidationPlugin
import amf.core.client.common.validation.ProfileName
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.validation.AMFValidationReport
import amf.core.internal.benchmark.ExecutionLog.log
import amf.core.internal.plugins.validation.ValidationOptions
import amf.core.internal.validation.core.ShaclValidationOptions
import amf.core.internal.validation.{EffectiveValidations, ShaclReportAdaptation}
import amf.validation.internal.shacl.ShaclValidator

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
