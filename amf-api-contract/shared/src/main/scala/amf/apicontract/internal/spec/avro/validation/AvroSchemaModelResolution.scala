package amf.apicontract.internal.spec.avro.validation

import amf.apicontract.internal.spec.avro.transformation.AvroSchemaTransformationPipeline
import amf.core.client.common.validation.ProfileName
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.transform.TransformationPipelineRunner
import amf.core.client.scala.validation.AMFValidationReport
import amf.core.internal.validation.ValidationConfiguration

object AvroSchemaModelResolution {

  def withResolvedModel[T](unit: BaseUnit, profile: ProfileName, conf: ValidationConfiguration)(
      withResolved: (BaseUnit, Option[AMFValidationReport]) => T
  ): T = {
    if (unit.processingData.transformed.is(true))
      withResolved(unit, None)
    else {
      val resolvedUnit =
        TransformationPipelineRunner(conf.eh, conf.amfConfig).run(unit, AvroSchemaTransformationPipeline())
      withResolved(resolvedUnit, Some(AMFValidationReport(resolvedUnit.id, profile, conf.eh.getResults)))
    }
  }

}
