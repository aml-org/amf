package amf.apicontract.internal.transformation

import amf.apicontract.internal.spec.common.transformation.stage.{
  OpenApiParametersNormalizationStage,
  ParametersNormalizationStage
}
import amf.apicontract.internal.transformation.stages.WebApiReferenceResolutionStage
import amf.core.client.common.transform._
import amf.core.client.common.validation.{ProfileName, Raml08Profile}
import amf.core.internal.remote.Raml08

class Raml08TransformationPipeline private (override val name: String) extends AmfTransformationPipeline(name) {
  override def profileName: ProfileName = Raml08Profile
  override def references               = new WebApiReferenceResolutionStage()

  override protected def parameterNormalizationStage: ParametersNormalizationStage =
    new OpenApiParametersNormalizationStage()
}

object Raml08TransformationPipeline {
  def apply()      = new Raml08TransformationPipeline(name)
  val name: String = PipelineId.Default
}
