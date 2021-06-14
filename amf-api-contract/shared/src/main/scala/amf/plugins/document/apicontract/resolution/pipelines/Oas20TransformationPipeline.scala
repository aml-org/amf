package amf.plugins.document.apicontract.resolution.pipelines

import amf.core.client.common.validation.{Oas20Profile, ProfileName}
import amf.core.client.scala.transform.PipelineName
import amf.core.client.scala.transform.pipelines.TransformationPipeline
import amf.core.internal.remote.Oas20
import amf.plugins.domain.apicontract.resolution.stages.{
  OpenApiParametersNormalizationStage,
  ParametersNormalizationStage
}

class Oas20TransformationPipeline private (override val name: String) extends AmfTransformationPipeline(name) {
  override def profileName: ProfileName = Oas20Profile
  override def references               = new WebApiReferenceResolutionStage()

  override protected def parameterNormalizationStage: ParametersNormalizationStage =
    new OpenApiParametersNormalizationStage()
}

object Oas20TransformationPipeline {
  def apply()      = new Oas20TransformationPipeline(name)
  val name: String = PipelineName.from(Oas20.name, TransformationPipeline.DEFAULT_PIPELINE)
}
