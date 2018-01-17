package amf.plugins.document.webapi.resolution.pipelines

import amf.ProfileNames
import amf.core.model.document.BaseUnit
import amf.plugins.domain.webapi.resolution.stages.{
  ExamplesResolutionStage,
  MediaTypeResolutionStage,
  ParametersNormalizationStage
}

class OasResolutionPipeline extends AmfResolutionPipeline {
  override val parameters = new ParametersNormalizationStage(ProfileNames.OAS)
  override val mediaTypes = new MediaTypeResolutionStage(ProfileNames.OAS)
}
