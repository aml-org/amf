package amf.apicontract.internal.validation.plugin

import amf.apicontract.internal.transformation.ValidationTransformationPipeline
import amf.core.client.common.validation.ProfileName
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.validation.AMFValidationReport
import amf.core.internal.validation.ValidationConfiguration

trait ModelResolution {

  def withResolvedModel[T](unit: BaseUnit, profile: ProfileName, conf: ValidationConfiguration)(
      withResolved: (BaseUnit, Option[AMFValidationReport]) => T
  ): T = {
    if (unit.processingData.transformed.is(true)) withResolved(unit, None)
    else {
      val resolvedUnit = ValidationTransformationPipeline(profile, unit, conf)
      withResolved(resolvedUnit, Some(AMFValidationReport(resolvedUnit.id, profile, conf.eh.getResults)))
    }
  }
}
