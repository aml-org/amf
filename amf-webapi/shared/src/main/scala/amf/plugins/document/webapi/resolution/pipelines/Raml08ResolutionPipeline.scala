package amf.plugins.document.webapi.resolution.pipelines

import amf.client.remod.amfcore.resolution.PipelineName
import amf.core.errorhandling.ErrorHandler
import amf.core.remote.Raml08
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.plugins.domain.webapi.resolution.stages.{OpenApiParametersNormalizationStage, ParametersNormalizationStage}
import amf.{ProfileName, Raml08Profile}

class Raml08ResolutionPipeline private (override val name: String) extends AmfResolutionPipeline(name) {
  override def profileName: ProfileName = Raml08Profile
  override def references               = new WebApiReferenceResolutionStage()

  override protected def parameterNormalizationStage: ParametersNormalizationStage =
    new OpenApiParametersNormalizationStage()
}

object Raml08ResolutionPipeline {
  def apply()      = new Raml08ResolutionPipeline(name)
  val name: String = PipelineName.from(Raml08.name, ResolutionPipeline.DEFAULT_PIPELINE)
}
