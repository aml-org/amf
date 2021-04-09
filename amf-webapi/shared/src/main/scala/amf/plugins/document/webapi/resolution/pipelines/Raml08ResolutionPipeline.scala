package amf.plugins.document.webapi.resolution.pipelines

import amf.core.errorhandling.ErrorHandler
import amf.plugins.domain.webapi.resolution.stages.{OpenApiParametersNormalizationStage, ParametersNormalizationStage}
import amf.{ProfileName, Raml08Profile}

class Raml08ResolutionPipeline() extends AmfResolutionPipeline() {
  override def profileName: ProfileName              = Raml08Profile
  override def references(implicit eh: ErrorHandler) = new WebApiReferenceResolutionStage()

  override protected def parameterNormalizationStage(implicit eh: ErrorHandler): ParametersNormalizationStage =
    new OpenApiParametersNormalizationStage()
}
