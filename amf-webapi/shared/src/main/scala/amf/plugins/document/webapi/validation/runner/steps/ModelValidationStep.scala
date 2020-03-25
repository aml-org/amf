package amf.plugins.document.webapi.validation.runner.steps

import amf.core.benchmark.ExecutionLog
import amf.core.services.RuntimeValidator
import amf.core.validation.AMFValidationResult
import amf.core.validation.core.ValidationResult
import amf.plugins.document.webapi.validation.runner.{FilterDataNodeOptions, ValidationContext}
import amf.validations.CustomShaclFunctions
import scala.concurrent.ExecutionContext.Implicits.global
import amf._

import scala.concurrent.Future

case class ModelValidationStep(override val validationContext: ValidationContext) extends ValidationStep {

  override protected def validate(): Future[Seq[AMFValidationResult]] = {
    val baseOptions = FilterDataNodeOptions().withMessageStyle(validationContext.messageStyle)
    val options = validationContext.profile match {
      case RamlProfile | Raml10Profile | Raml08Profile | OasProfile | Oas20Profile | Oas30Profile | AsyncProfile |
          Async20Profile | AmfProfile =>
        baseOptions.withPartialValidation()
      case _ =>
        baseOptions.withFullValidation()
    }
    ExecutionLog.log("WebApiValidations#validationRequestsForBaseUnit: validating now WebAPI")
    RuntimeValidator
      .shaclValidation(validationContext.baseUnit,
                       validationContext.validations,
                       CustomShaclFunctions.functions,
                       options)
      .map { report =>
        report.results.flatMap {
          buildValidationResult
        }
      }
  }

  override def endStep: Boolean = true

  private def buildValidationResult(r: ValidationResult): Option[AMFValidationResult] = {
    buildValidationResult(validationContext.baseUnit, r, validationContext.messageStyle, validationContext.validations)
  }
}
