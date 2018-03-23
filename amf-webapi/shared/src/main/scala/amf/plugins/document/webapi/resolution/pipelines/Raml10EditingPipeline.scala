package amf.plugins.document.webapi.resolution.pipelines

import amf.ProfileNames
import amf.plugins.domain.webapi.resolution.stages.{ExamplesResolutionStage, MediaTypeResolutionStage, ParametersNormalizationStage}

class Raml10EditingPipeline extends AmfEditingPipeline {
  override val parameters = new ParametersNormalizationStage(ProfileNames.RAML)
  override val mediaTypes = new MediaTypeResolutionStage(ProfileNames.RAML)
  override val examples   = new ExamplesResolutionStage(ProfileNames.RAML)
}
