package amf.plugins.document.webapi.resolution.pipelines

import amf.ProfileNames
import amf.plugins.domain.webapi.resolution.stages.{MediaTypeResolutionStage, ParametersNormalizationStage}

class OasEditingPipeline extends AmfEditingPipeline {
  override val references = new OasReferenceResolutionStage()
  override val parameters = new ParametersNormalizationStage(ProfileNames.OAS)
  override val mediaTypes = new MediaTypeResolutionStage(ProfileNames.OAS)
}
