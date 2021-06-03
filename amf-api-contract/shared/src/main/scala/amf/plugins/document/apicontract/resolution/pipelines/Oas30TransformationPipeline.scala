package amf.plugins.document.apicontract.resolution.pipelines
import amf.client.remod.amfcore.resolution.PipelineName
import amf.core.errorhandling.AMFErrorHandler
import amf.core.remote.Oas30
import amf.core.resolution.pipelines.TransformationPipeline
import amf.core.resolution.stages.TransformationStep
import amf.plugins.domain.shapes.resolution.stages.RequestParamsLinkStage
import amf.plugins.domain.apicontract.resolution.stages.{
  OpenApiParametersNormalizationStage,
  ParametersNormalizationStage
}
import amf.{Oas30Profile, ProfileName}

class Oas30TransformationPipeline private (override val name: String) extends AmfTransformationPipeline(name) {
  override def profileName: ProfileName = Oas30Profile
  override def references               = new WebApiReferenceResolutionStage()

  override protected def parameterNormalizationStage: ParametersNormalizationStage =
    new OpenApiParametersNormalizationStage()

  override def steps: Seq[TransformationStep] = Seq(RequestParamsLinkStage) ++ super.steps
}

object Oas30TransformationPipeline {
  def apply()      = new Oas30TransformationPipeline(name)
  val name: String = PipelineName.from(Oas30.name, TransformationPipeline.DEFAULT_PIPELINE)
}
