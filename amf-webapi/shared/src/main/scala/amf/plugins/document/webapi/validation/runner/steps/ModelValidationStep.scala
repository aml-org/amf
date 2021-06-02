package amf.plugins.document.webapi.validation.runner.steps

import amf._
import amf.core.benchmark.ExecutionLog.log
import amf.core.services.ValidationOptions
import amf.core.validation.{AMFValidationReport, ShaclReportAdaptation}
import amf.plugins.document.webapi.validation.runner.{FilterDataNodeOptions, ValidationContext}
import amf.plugins.features.validation.ShaclValidationRunner
import amf.validations.CustomShaclFunctions

import scala.concurrent.{ExecutionContext, Future}

case class ModelValidationStep(override val validationContext: ValidationContext)
    extends ValidationStep
    with ShaclReportAdaptation {

  override protected def validate()(implicit executionContext: ExecutionContext): Future[AMFValidationReport] = {
    val baseOptions                = FilterDataNodeOptions().withMessageStyle(validationContext.profileName.messageStyle)
    val options: ValidationOptions = computeValidationExtent(baseOptions)
    log("WebApiValidations#validationRequestsForBaseUnit: validating now WebAPI")
    ShaclValidationRunner
      .validate(validationContext.baseUnit, validationContext.validations, CustomShaclFunctions.functions, options)
      .map { report =>
        adaptToAmfReport(validationContext.baseUnit,
                         validationContext.profileName,
                         report,
                         validationContext.validations)
      }
  }

  private def computeValidationExtent(baseOptions: ValidationOptions) = {
    val options = validationContext.profileName match {
      case Raml10Profile | Raml08Profile | Oas20Profile | Oas30Profile | AsyncProfile | Async20Profile | AmfProfile =>
        baseOptions.withPartialValidation()
      case _ =>
        baseOptions.withFullValidation()
    }
    options
  }
}
