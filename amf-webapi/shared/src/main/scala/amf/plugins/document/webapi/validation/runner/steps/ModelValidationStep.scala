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

case class ModelValidationStep(override val validationContext: ValidationContext)(
    implicit executionContext: ExecutionContext)
    extends ValidationStep
    with ShaclReportAdaptation {

  override protected def validate(): Future[AMFValidationReport] = {
    val baseOptions                = FilterDataNodeOptions().withMessageStyle(validationContext.messageStyle)
    val options: ValidationOptions = computeValidationExtent(baseOptions)
    log("WebApiValidations#validationRequestsForBaseUnit: validating now WebAPI")
    RuntimeValidator
      .shaclValidation(validationContext.baseUnit,
                       validationContext.validations,
                       CustomShaclFunctions.functions,
                       options)
      .map { report =>
        adaptToAmfReport(validationContext.baseUnit,
                         validationContext.profile,
                         report,
                         validationContext.messageStyle,
                         validationContext.validations)
      }
  }

  private def computeValidationExtent(baseOptions: ValidationOptions) = {
    val options = validationContext.profile match {
      case RamlProfile | Raml10Profile | Raml08Profile | OasProfile | Oas20Profile | Oas30Profile | AsyncProfile |
          Async20Profile | AmfProfile =>
        baseOptions.withPartialValidation()
      case _ =>
        baseOptions.withFullValidation()
    }
    options
  }

  override def endStep: Boolean = true

  private def buildValidationResult(r: ValidationResult): Option[AMFValidationResult] = {
    adaptToAmfResult(validationContext.baseUnit, r, validationContext.messageStyle, validationContext.validations)
  }
}
