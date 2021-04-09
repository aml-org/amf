package amf.plugins.document.webapi.resolution.pipelines
import amf.core.errorhandling.ErrorHandler
import amf.plugins.domain.webapi.resolution.stages.{OpenApiParametersNormalizationStage, ParametersNormalizationStage}
import amf.{ProfileName, Raml08Profile}

class Raml08EditingPipeline(urlShortening: Boolean = true) extends AmfEditingPipeline(urlShortening) {
  override def profileName: ProfileName              = Raml08Profile
  override def references(implicit eh: ErrorHandler) = new WebApiReferenceResolutionStage(true)

  override def parameterNormalizationStage(implicit eh: ErrorHandler): ParametersNormalizationStage =
    new OpenApiParametersNormalizationStage()
}
