package amf.plugins.document.webapi.resolution.pipelines
import amf.core.errorhandling.ErrorHandler
import amf.plugins.domain.webapi.resolution.stages.{OpenApiParametersNormalizationStage, ParametersNormalizationStage}
import amf.{OasProfile, ProfileName}

class OasEditingPipeline(urlShortening: Boolean = true) extends AmfEditingPipeline(urlShortening) {
  override def profileName: ProfileName              = OasProfile
  override def references(implicit eh: ErrorHandler) = new WebApiReferenceResolutionStage(true)

  override def parameterNormalizationStage(implicit eh: ErrorHandler): ParametersNormalizationStage =
    new OpenApiParametersNormalizationStage()
}
