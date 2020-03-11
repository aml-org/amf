package amf.plugins.document.webapi.resolution.pipelines
import amf.core.errorhandling.ErrorHandler
import amf.plugins.domain.webapi.resolution.stages.{OpenApiParametersNormalizationStage, ParametersNormalizationStage}
import amf.{Oas30Profile, ProfileName}

class Oas30ResolutionPipeline(override val eh: ErrorHandler) extends AmfResolutionPipeline(eh) {
  override def profileName: ProfileName = Oas30Profile
  override def references               = new WebApiReferenceResolutionStage()

  override protected def parameterNormalizationStage: ParametersNormalizationStage =
    new OpenApiParametersNormalizationStage()
}
