package amf.plugins.document.apicontract.resolution.pipelines

import amf.core.client.common.validation.{Oas30Profile, ProfileName}
import amf.core.client.scala.transform.PipelineName
import amf.core.client.scala.transform.pipelines.TransformationPipeline
import amf.core.client.scala.transform.stages.TransformationStep
import amf.core.internal.remote.Oas30
import amf.plugins.domain.apicontract.resolution.stages.{
  OpenApiParametersNormalizationStage,
  ParametersNormalizationStage
}
import amf.plugins.domain.shapes.resolution.stages.RequestParamsLinkStage

class Oas30TransformationPipeline private (override val name: String) extends AmfTransformationPipeline(name) {
  override def profileName: ProfileName = Oas30Profile
  override def references               = new WebApiReferenceResolutionStage()

  override protected def parameterNormalizationStage: ParametersNormalizationStage =
    new OpenApiParametersNormalizationStage()

  override def steps: Seq[TransformationStep] = Seq(RequestParamsLinkStage) ++ super.steps
}

object Oas30TransformationPipeline {
  def apply()      = new Oas30TransformationPipeline(name)
  val name: String = PipelineName.from(Oas30.mediaType, TransformationPipeline.DEFAULT_PIPELINE)
}
