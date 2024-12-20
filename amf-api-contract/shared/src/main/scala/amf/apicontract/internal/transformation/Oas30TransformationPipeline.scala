package amf.apicontract.internal.transformation

import amf.apicontract.internal.spec.common.transformation.stage.{
  OpenApiParametersNormalizationStage,
  ParametersNormalizationStage,
  RequestParamsLinkStage
}
import amf.apicontract.internal.transformation.stages.WebApiReferenceResolutionStage
import amf.core.client.common.transform._
import amf.core.client.common.validation.{Oas30Profile, ProfileName}
import amf.core.client.scala.transform.TransformationStep

class Oas30TransformationPipeline private[amf] (override val name: String) extends AmfTransformationPipeline(name) {
  override def profileName: ProfileName = Oas30Profile
  override def references               = new WebApiReferenceResolutionStage()

  override protected def parameterNormalizationStage: ParametersNormalizationStage =
    new OpenApiParametersNormalizationStage()

  override def steps: Seq[TransformationStep] = RequestParamsLinkStage +: super.steps
}

object Oas30TransformationPipeline {
  def apply()      = new Oas30TransformationPipeline(name)
  val name: String = PipelineId.Default
}
