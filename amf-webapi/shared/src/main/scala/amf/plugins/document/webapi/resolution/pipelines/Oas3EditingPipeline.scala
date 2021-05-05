package amf.plugins.document.webapi.resolution.pipelines
import amf.client.remod.amfcore.resolution.PipelineName
import amf.core.errorhandling.ErrorHandler
import amf.core.remote.Oas30
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.shapes.resolution.stages.RequestParamsLinkStage
import amf.plugins.domain.webapi.resolution.stages.{OpenApiParametersNormalizationStage, ParametersNormalizationStage}
import amf.{Oas30Profile, ProfileName}

class Oas3EditingPipeline private(urlShortening: Boolean, override val name:String)
    extends AmfEditingPipeline(urlShortening, name) {
  override def profileName: ProfileName = Oas30Profile
  override def references(implicit eh: ErrorHandler)               = new WebApiReferenceResolutionStage(true)

  override def parameterNormalizationStage(implicit eh: ErrorHandler): ParametersNormalizationStage = new OpenApiParametersNormalizationStage()

  override def steps(implicit eh: ErrorHandler): Seq[ResolutionStage] = Seq(
    new RequestParamsLinkStage(),
  ) ++ super.steps
}

object Oas3EditingPipeline{
  val name:String = PipelineName.from(Oas30.name, ResolutionPipeline.EDITING_PIPELINE)
  def apply() = new Oas3EditingPipeline(true, name = name)

  private[amf] def cachePipeline() = new Oas3EditingPipeline(false, Oas3CachePipeline.name)
}

object Oas3CachePipeline{
  val name:String = PipelineName.from(Oas30.name, ResolutionPipeline.CACHE_PIPELINE)
  def apply(): Oas3EditingPipeline = Oas3EditingPipeline.cachePipeline()
}
