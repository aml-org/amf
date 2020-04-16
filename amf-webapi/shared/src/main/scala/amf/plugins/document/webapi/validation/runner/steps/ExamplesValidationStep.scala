package amf.plugins.document.webapi.validation.runner.steps

import amf.core.validation.AMFValidationResult
import amf.plugins.document.webapi.validation.UnitPayloadsValidation
import amf.plugins.document.webapi.validation.runner.ValidationContext

import scala.concurrent.{ExecutionContext, Future}

case class ExamplesValidationStep(override val validationContext: ValidationContext)(
    implicit executionContext: ExecutionContext)
    extends ValidationStep {

  override protected def validate(): Future[Seq[AMFValidationResult]] = {
    UnitPayloadsValidation(validationContext.baseUnit, validationContext.platform)
      .validate(validationContext.env)
      .map { results =>
        results.flatMap {
          buildValidationWithCustomLevelForProfile
        }
      }
  }

  override def endStep: Boolean = true

  private def buildValidationWithCustomLevelForProfile(result: AMFValidationResult): Option[AMFValidationResult] = {
    Some(result.copy(level = findLevel(result.validationId, validationContext.validations)))
  }
}
