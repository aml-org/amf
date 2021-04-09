package amf.plugins.document.webapi.resolution.pipelines

import amf.core.errorhandling.ErrorHandler
import amf.plugins.domain.webapi.resolution.stages._
import amf.{ProfileName, RamlProfile}

class Raml10EditingPipeline(urlShortening: Boolean = true) extends AmfEditingPipeline(urlShortening) {
  override def profileName: ProfileName              = RamlProfile
  override def references(implicit eh: ErrorHandler) = new WebApiReferenceResolutionStage(true)

  override def parameterNormalizationStage(implicit eh: ErrorHandler): ParametersNormalizationStage =
    new Raml10ParametersNormalizationStage()
}
