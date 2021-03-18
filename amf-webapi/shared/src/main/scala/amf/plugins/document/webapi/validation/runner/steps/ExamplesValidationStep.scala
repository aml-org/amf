package amf.plugins.document.webapi.validation.runner.steps

import amf.core.validation.{AMFValidationReport, AMFValidationResult, ShaclReportAdaptation}
import amf.plugins.document.webapi.validation.UnitPayloadsValidation
import amf.plugins.document.webapi.validation.runner.ValidationContext

import scala.concurrent.{ExecutionContext, Future}

case class ExamplesValidationStep(override val validationContext: ValidationContext)(
    implicit executionContext: ExecutionContext)
    extends ValidationStep
    with ShaclReportAdaptation {

  override protected def validate(): Future[AMFValidationReport] = {
    UnitPayloadsValidation(validationContext.baseUnit, validationContext.platform)
      .validate(validationContext.env)
      .map { results =>
        val mappedSeverityResults = results.flatMap { buildValidationWithCustomLevelForProfile }
        AMFValidationReport(validationContext.baseUnit.id, validationContext.profile, mappedSeverityResults)
      }
  }

  override def endStep: Boolean = true

  private def buildValidationWithCustomLevelForProfile(result: AMFValidationResult): Option[AMFValidationResult] = {
    Some(result.copy(level = findLevel(result.validationId, validationContext.validations)))
  }
}
