package amf.plugins.document.webapi.resolution.pipelines
import amf.client.remod.amfcore.resolution.PipelineName
import amf.core.errorhandling.ErrorHandler
import amf.core.remote.Oas20
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.plugins.domain.webapi.resolution.stages.{OpenApiParametersNormalizationStage, ParametersNormalizationStage}
import amf.{Oas20Profile, ProfileName}

class Oas20ResolutionPipeline private (override val name: String) extends AmfResolutionPipeline(name) {
  override def profileName: ProfileName = Oas20Profile
  override def references               = new WebApiReferenceResolutionStage()

  override protected def parameterNormalizationStage: ParametersNormalizationStage =
    new OpenApiParametersNormalizationStage()
}

object Oas20ResolutionPipeline {
  def apply()      = new Oas20ResolutionPipeline(name)
  val name: String = PipelineName.from(Oas20.name, ResolutionPipeline.DEFAULT_PIPELINE)
}
