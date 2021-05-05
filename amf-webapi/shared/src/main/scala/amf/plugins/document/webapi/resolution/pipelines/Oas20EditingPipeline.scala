package amf.plugins.document.webapi.resolution.pipelines
import amf.client.remod.amfcore.resolution.PipelineName
import amf.core.errorhandling.ErrorHandler
import amf.core.remote.Oas20
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.plugins.document.webapi.resolution.pipelines.Oas20EditingPipeline.cachePipeline
import amf.plugins.domain.webapi.resolution.stages.{OpenApiParametersNormalizationStage, ParametersNormalizationStage}
import amf.{Oas20Profile, ProfileName}

class Oas20EditingPipeline private (urlShortening: Boolean, override val name: String)
    extends AmfEditingPipeline(urlShortening, name) {
  override def profileName: ProfileName              = Oas20Profile
  override def references(implicit eh: ErrorHandler) = new WebApiReferenceResolutionStage(true)

  override def parameterNormalizationStage(implicit eh: ErrorHandler): ParametersNormalizationStage =
    new OpenApiParametersNormalizationStage()
}

object Oas20EditingPipeline {
  def apply()                    = new Oas20EditingPipeline(true, name)
  private[amf] def cachePipeline = new Oas20EditingPipeline(false, Oas20CachePipeline.name)
  val name: String               = PipelineName.from(Oas20.name, ResolutionPipeline.EDITING_PIPELINE)
}

object Oas20CachePipeline {
  def apply(): Oas20EditingPipeline = cachePipeline
  val name: String                  = PipelineName.from(Oas20.name, ResolutionPipeline.CACHE_PIPELINE)
}
