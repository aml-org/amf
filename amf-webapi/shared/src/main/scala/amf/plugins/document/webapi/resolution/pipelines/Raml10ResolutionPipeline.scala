package amf.plugins.document.webapi.resolution.pipelines

import amf.ProfileNames
import amf.plugins.domain.webapi.resolution.stages.{ExamplesResolutionStage, ParametersNormalizationStage}

class Raml10ResolutionPipeline extends AmfResolutionPipeline {
  override val profileName: String = ProfileNames.RAML
  override val parameters = new ParametersNormalizationStage(ProfileNames.RAML)
  override val examples   = new ExamplesResolutionStage(ProfileNames.RAML)
}
