package amf.plugins.document.webapi.resolution.pipelines
import amf.client.remod.amfcore.resolution.PipelineName
import amf.core.errorhandling.ErrorHandler
import amf.core.remote.Oas30
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.shapes.resolution.stages.RequestParamsLinkStage
import amf.plugins.domain.webapi.resolution.stages.{OpenApiParametersNormalizationStage, ParametersNormalizationStage}
import amf.{Oas30Profile, ProfileName}

class Oas30ResolutionPipeline private (override val name: String) extends AmfResolutionPipeline(name) {
  override def profileName: ProfileName = Oas30Profile
  override def references(implicit eh: ErrorHandler)               = new WebApiReferenceResolutionStage()

  override protected def parameterNormalizationStage(implicit eh: ErrorHandler): ParametersNormalizationStage =
    new OpenApiParametersNormalizationStage()

  override def steps(implicit eh: ErrorHandler): Seq[ResolutionStage] = Seq(
    new RequestParamsLinkStage(),
  ) ++ super.steps(eh)
}

object Oas30ResolutionPipeline{
  def apply() = new Oas30ResolutionPipeline(name)
  val name: String = PipelineName.from(Oas30.name, ResolutionPipeline.DEFAULT_PIPELINE)
}
