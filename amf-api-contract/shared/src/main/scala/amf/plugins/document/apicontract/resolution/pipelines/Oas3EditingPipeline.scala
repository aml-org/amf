package amf.plugins.document.apicontract.resolution.pipelines
import amf.client.remod.amfcore.resolution.PipelineName
import amf.core.errorhandling.AMFErrorHandler
import amf.core.remote.Oas30
import amf.core.resolution.pipelines.TransformationPipeline
import amf.core.resolution.stages.TransformationStep
import amf.plugins.domain.shapes.resolution.stages.RequestParamsLinkStage
import amf.plugins.domain.apicontract.resolution.stages.{OpenApiParametersNormalizationStage, ParametersNormalizationStage}
import amf.{Oas30Profile, ProfileName}

class Oas3EditingPipeline private(urlShortening: Boolean, override val name:String)
    extends AmfEditingPipeline(urlShortening, name) {
  override def profileName: ProfileName = Oas30Profile
  override def references               = new WebApiReferenceResolutionStage(true)

  override def parameterNormalizationStage: ParametersNormalizationStage = new OpenApiParametersNormalizationStage()

  override def steps: Seq[TransformationStep] = Seq(
    RequestParamsLinkStage,
  ) ++ super.steps
}

object Oas3EditingPipeline{
  val name:String = PipelineName.from(Oas30.name, TransformationPipeline.EDITING_PIPELINE)
  def apply() = new Oas3EditingPipeline(true, name = name)

  private[amf] def cachePipeline() = new Oas3EditingPipeline(false, Oas3CachePipeline.name)
}

object Oas3CachePipeline{
  val name:String = PipelineName.from(Oas30.name, TransformationPipeline.CACHE_PIPELINE)
  def apply(): Oas3EditingPipeline = Oas3EditingPipeline.cachePipeline()
}
