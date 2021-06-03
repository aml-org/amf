package amf.plugins.document.apicontract.resolution.pipelines
import amf.client.remod.amfcore.resolution.PipelineName
import amf.core.errorhandling.AMFErrorHandler
import amf.core.remote.Oas20
import amf.core.resolution.pipelines.TransformationPipeline
import amf.plugins.document.apicontract.resolution.pipelines.Oas20EditingPipeline.cachePipeline
import amf.plugins.domain.apicontract.resolution.stages.{
  OpenApiParametersNormalizationStage,
  ParametersNormalizationStage
}
import amf.{Oas20Profile, ProfileName}

class Oas20EditingPipeline private (urlShortening: Boolean, override val name: String)
    extends AmfEditingPipeline(urlShortening, name) {
  override def profileName: ProfileName = Oas20Profile
  override def references               = new WebApiReferenceResolutionStage(true)

  override def parameterNormalizationStage: ParametersNormalizationStage =
    new OpenApiParametersNormalizationStage()
}

object Oas20EditingPipeline {
  def apply()                    = new Oas20EditingPipeline(true, name)
  private[amf] def cachePipeline = new Oas20EditingPipeline(false, Oas20CachePipeline.name)
  val name: String               = PipelineName.from(Oas20.name, TransformationPipeline.EDITING_PIPELINE)
}

object Oas20CachePipeline {
  def apply(): Oas20EditingPipeline = cachePipeline
  val name: String                  = PipelineName.from(Oas20.name, TransformationPipeline.CACHE_PIPELINE)
}
