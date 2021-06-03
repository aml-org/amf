package amf.plugins.document.apicontract.resolution.pipelines

import amf.client.remod.amfcore.resolution.PipelineName
import amf.core.errorhandling.AMFErrorHandler
import amf.core.remote.Raml08
import amf.core.resolution.pipelines.TransformationPipeline
import amf.plugins.domain.apicontract.resolution.stages.{
  OpenApiParametersNormalizationStage,
  ParametersNormalizationStage
}
import amf.{ProfileName, Raml08Profile}

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
