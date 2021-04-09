package amf.plugins.document.webapi.resolution.pipelines

import amf.core.errorhandling.ErrorHandler
import amf.plugins.domain.webapi.resolution.stages._
import amf.{ProfileName, RamlProfile}

class Raml10ResolutionPipeline() extends AmfResolutionPipeline() {
  override def profileName: ProfileName              = RamlProfile
  override def references(implicit eh: ErrorHandler) = new WebApiReferenceResolutionStage()

  override protected def parameterNormalizationStage(implicit eh: ErrorHandler): ParametersNormalizationStage =
    new Raml10ParametersNormalizationStage()(eh)
}
