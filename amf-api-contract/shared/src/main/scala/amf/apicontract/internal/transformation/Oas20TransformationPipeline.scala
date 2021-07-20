package amf.apicontract.internal.transformation

import amf.apicontract.internal.spec.common.transformation.stage.{
  OpenApiParametersNormalizationStage,
  ParametersNormalizationStage
}
import amf.apicontract.internal.transformation.stages.WebApiReferenceResolutionStage
import amf.core.client.common.transform._
import amf.core.client.common.validation.{Oas20Profile, ProfileName}
import amf.core.internal.remote.Oas20

class Oas20TransformationPipeline private (override val name: String) extends AmfTransformationPipeline(name) {
  override def profileName: ProfileName = Oas20Profile
  override def references               = new WebApiReferenceResolutionStage()

  override protected def parameterNormalizationStage: ParametersNormalizationStage =
    new OpenApiParametersNormalizationStage()
}

object Oas20TransformationPipeline {
  def apply()      = new Oas20TransformationPipeline(name)
  val name: String = PipelineId.Default
}
