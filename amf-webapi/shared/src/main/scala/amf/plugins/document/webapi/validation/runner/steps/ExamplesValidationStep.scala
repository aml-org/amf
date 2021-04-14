package amf.plugins.document.webapi.validation.runner.steps

import amf.core.validation.{AMFValidationReport, AMFValidationResult, ShaclReportAdaptation}
import amf.plugins.document.webapi.validation.UnitPayloadsValidation
import amf.plugins.document.webapi.validation.collector.{
  EnumInShapesCollector,
  ExtensionsCollector,
  PayloadsInApiCollector,
  ShapeFacetsCollector
}
import amf.plugins.document.webapi.validation.runner.ValidationContext

import scala.concurrent.{ExecutionContext, Future}

case class ExamplesValidationStep(override val validationContext: ValidationContext)
    extends ValidationStep
    with ShaclReportAdaptation {

  override protected def validate()(implicit executionContext: ExecutionContext): Future[AMFValidationReport] = {
    val collectors = Seq(PayloadsInApiCollector, EnumInShapesCollector, ShapeFacetsCollector, ExtensionsCollector)
    UnitPayloadsValidation(validationContext.baseUnit, collectors)
      .validate(validationContext.env)
      .map { results =>
        val mappedSeverityResults = results.flatMap { buildValidationWithCustomLevelForProfile }
        AMFValidationReport(validationContext.baseUnit.id, validationContext.profile, mappedSeverityResults)
      }
  }

  private def buildValidationWithCustomLevelForProfile(result: AMFValidationResult): Option[AMFValidationResult] = {
    Some(result.copy(severityLevel = findLevel(result.validationId, validationContext.validations)))
  }
}
