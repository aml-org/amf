package amf.plugins.document.apicontract.resolution.pipelines
import amf.client.remod.amfcore.resolution.PipelineName
import amf.core.errorhandling.AMFErrorHandler
import amf.core.remote.Oas20
import amf.core.resolution.pipelines.TransformationPipeline
import amf.plugins.domain.apicontract.resolution.stages.{
  OpenApiParametersNormalizationStage,
  ParametersNormalizationStage
}
import amf.{Oas20Profile, ProfileName}

class Oas20TransformationPipeline private (override val name: String) extends AmfTransformationPipeline(name) {
  override def profileName: ProfileName = Oas20Profile
  override def references               = new WebApiReferenceResolutionStage()

  override protected def parameterNormalizationStage: ParametersNormalizationStage =
    new OpenApiParametersNormalizationStage()
}

object Oas20TransformationPipeline {
  def apply()      = new Oas20TransformationPipeline(name)
  val name: String = PipelineName.from(Oas20.name, TransformationPipeline.DEFAULT_PIPELINE)
}
