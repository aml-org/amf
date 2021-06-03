package amf.plugins.document.apicontract.validation.plugins

import amf.ProfileName
import amf.client.remod.amfcore.plugins.validate.ValidationConfiguration
import amf.core.model.document.BaseUnit
import amf.core.validation.AMFValidationReport
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
