package amf.shapes.internal.validation.payload

import amf.core.client.common.validation.ProfileName
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.validation.{AMFValidationReport, AMFValidationResult}
import amf.core.internal.plugins.validation.ValidationOptions
import amf.core.internal.validation.{EffectiveValidations, ShaclReportAdaptation}
import amf.shapes.internal.validation.payload.collector.ValidationCandidateCollector
import amf.shapes.internal.validation.plugin.BaseModelValidationPlugin

import scala.concurrent.{ExecutionContext, Future}

trait BasePayloadValidationPlugin extends BaseModelValidationPlugin with ShaclReportAdaptation {

  val profile: ProfileName

  val collectors: Seq[ValidationCandidateCollector]

  override protected def specificValidate(unit: BaseUnit, options: ValidationOptions)(implicit
      executionContext: ExecutionContext
  ): Future[AMFValidationReport] = {

    val validations = effectiveOrException(options.config, profile)

    UnitPayloadsValidation(unit, collectors)
      .validate(options.config)
      .map { results =>
        val mappedSeverityResults = results.flatMap { result =>
          buildValidationWithCustomLevelForProfile(result, validations)
        }
        AMFValidationReport(unit.id, profile, mappedSeverityResults)
      }
  }

  private def buildValidationWithCustomLevelForProfile(
      result: AMFValidationResult,
      validations: EffectiveValidations
  ): Option[AMFValidationResult] = {
    Some(result.copy(severityLevel = findLevel(result.validationId, validations)))
  }
}
