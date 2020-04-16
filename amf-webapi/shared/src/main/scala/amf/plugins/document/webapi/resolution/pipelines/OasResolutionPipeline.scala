package amf.plugins.document.webapi.resolution.pipelines
import amf.core.errorhandling.ErrorHandler
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.shapes.resolution.stages.TypeAliasTransformationStage
import amf.plugins.domain.webapi.resolution.stages.{OpenApiParametersNormalizationStage, ParametersNormalizationStage}
import amf.{OasProfile, ProfileName}

class OasResolutionPipeline(override val eh: ErrorHandler) extends AmfResolutionPipeline(eh) {
  override def profileName: ProfileName = OasProfile
  override def references               = new WebApiReferenceResolutionStage()

  override protected def parameterNormalizationStage: ParametersNormalizationStage =
    new OpenApiParametersNormalizationStage()

  override val steps: Seq[ResolutionStage] = Seq(
    new TypeAliasTransformationStage(),
  ) ++ baseSteps
}
