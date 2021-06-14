package amf.plugins.document.apicontract.validation.plugins

import amf.core.client.common.validation.ProfileName
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.validation.AMFValidationReport
import amf.core.internal.validation.ValidationConfiguration
import amf.plugins.document.apicontract.resolution.pipelines.ValidationTransformationPipeline

trait ModelResolution {

  def withResolvedModel[T](unit: BaseUnit, profile: ProfileName, conf: ValidationConfiguration)(
      withResolved: (BaseUnit, Option[AMFValidationReport]) => T): T = {
    if (unit.resolved) withResolved(unit, None)
    else {
      val resolvedUnit = ValidationTransformationPipeline(profile, unit, conf.eh)
      withResolved(resolvedUnit, Some(AMFValidationReport(resolvedUnit.id, profile, conf.eh.getResults)))
    }
  }
}
