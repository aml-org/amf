package amf.shapes.internal.validation.plugin

import amf.core.client.common.validation.ProfileName
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.transform.TransformationPipelineRunner
import amf.core.client.scala.validation.AMFValidationReport
import amf.core.internal.validation.ValidationConfiguration
import amf.shapes.internal.transformation.JsonSchemaTransformationPipeline

object JsonSchemaModelResolution {

  def withResolvedModel[T](unit: BaseUnit, profile: ProfileName, conf: ValidationConfiguration)(
      withResolved: (BaseUnit, Option[AMFValidationReport]) => T
  ): T = {
    if (unit.processingData.transformed.is(true)) withResolved(unit, None)
    else {
      val resolvedUnit =
        TransformationPipelineRunner(conf.eh, conf.amfConfig).run(unit, JsonSchemaTransformationPipeline())
      withResolved(resolvedUnit, Some(AMFValidationReport(resolvedUnit.id, profile, conf.eh.getResults)))
    }
  }

}
