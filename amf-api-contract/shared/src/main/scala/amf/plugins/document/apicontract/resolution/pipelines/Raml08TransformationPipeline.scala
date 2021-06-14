package amf.plugins.document.apicontract.resolution.pipelines

import amf.core.client.common.validation.{ProfileName, Raml08Profile}
import amf.core.client.scala.transform.PipelineName
import amf.core.client.scala.transform.pipelines.TransformationPipeline
import amf.core.internal.remote.Raml08
import amf.plugins.domain.apicontract.resolution.stages.{
  OpenApiParametersNormalizationStage,
  ParametersNormalizationStage
}

class Raml08TransformationPipeline private (override val name: String) extends AmfTransformationPipeline(name) {
  override def profileName: ProfileName = Raml08Profile
  override def references               = new WebApiReferenceResolutionStage()

  override protected def parameterNormalizationStage: ParametersNormalizationStage =
    new OpenApiParametersNormalizationStage()
}

object Raml08TransformationPipeline {
  def apply()      = new Raml08TransformationPipeline(name)
  val name: String = PipelineName.from(Raml08.name, TransformationPipeline.DEFAULT_PIPELINE)
}
