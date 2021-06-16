package amf.apicontract.internal.validation.payload

import amf.apicontract.internal.validation.payload.collector.{EnumInShapesCollector, ExtensionsCollector, PayloadsInApiCollector, ShapeFacetsCollector}
import amf.apicontract.internal.validation.plugin.{AmlAware, BaseApiValidationPlugin}
import amf.core.client.common.validation.ProfileName
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.validation.{AMFValidationReport, AMFValidationResult}
import amf.core.internal.plugins.validation.{ValidationInfo, ValidationOptions}
import amf.core.internal.validation.{EffectiveValidations, ShaclReportAdaptation}

import scala.concurrent.{ExecutionContext, Future}

object PayloadValidationPlugin {

  protected val id: String = this.getClass.getSimpleName

  def apply() = new PayloadValidationPlugin()
}

class PayloadValidationPlugin extends BaseApiValidationPlugin with AmlAware with ShaclReportAdaptation {
  override val id: String = PayloadValidationPlugin.id

  override def applies(element: ValidationInfo): Boolean = super.applies(element)

  override protected def specificValidate(unit: BaseUnit, profile: ProfileName, options: ValidationOptions)(
      implicit executionContext: ExecutionContext): Future[AMFValidationReport] = {
    val collectors = Seq(PayloadsInApiCollector, EnumInShapesCollector, ShapeFacetsCollector, ExtensionsCollector)
    UnitPayloadsValidation(unit, collectors)
      .validate(options.config)
      .map { results =>
        val mappedSeverityResults = results.flatMap { result =>
          buildValidationWithCustomLevelForProfile(result, options.effectiveValidations)
        }
        AMFValidationReport(unit.id, profile, mappedSeverityResults)
      }
  }

  private def buildValidationWithCustomLevelForProfile(
      result: AMFValidationResult,
      validations: EffectiveValidations): Option[AMFValidationResult] = {
    Some(result.copy(severityLevel = findLevel(result.validationId, validations)))
  }
}
