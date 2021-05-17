package amf.plugins.document.webapi.validation.runner.steps

import amf.core.benchmark.ExecutionLog
import amf.core.services.{RuntimeValidator, ValidationOptions}
import amf.core.validation.{AMFValidationReport, AMFValidationResult, ShaclReportAdaptation}
import amf.core.validation.core.ValidationResult
import amf.plugins.document.webapi.validation.runner.{FilterDataNodeOptions, ValidationContext}
import amf.validations.CustomShaclFunctions
import amf._
import amf.core.benchmark.ExecutionLog.log

import scala.concurrent.{ExecutionContext, Future}

case class ModelValidationStep(override val validationContext: ValidationContext)
    extends ValidationStep
    with ShaclReportAdaptation {

  override protected def validate()(implicit executionContext: ExecutionContext): Future[AMFValidationReport] = {
    val baseOptions                = FilterDataNodeOptions().withMessageStyle(validationContext.profileName.messageStyle)
    val options: ValidationOptions = computeValidationExtent(baseOptions)
    log("WebApiValidations#validationRequestsForBaseUnit: validating now WebAPI")
    RuntimeValidator
      .shaclValidation(validationContext.baseUnit,
                       validationContext.validations,
                       CustomShaclFunctions.functions,
                       options)
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
