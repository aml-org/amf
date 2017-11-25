package amf.plugins.document.webapi.resolution.pipelines

import amf.ProfileNames
import amf.core.model.document.BaseUnit
import amf.plugins.document.graph.resolution.pipelines.AmfResolutionPipeline
import amf.plugins.domain.webapi.resolution.stages.{ExamplesResolutionStage, MediaTypeResolutionStage, ParametersNormalizationStage}

class RamlResolutionPipeline extends AmfResolutionPipeline {

  override val parameters = new ParametersNormalizationStage(ProfileNames.RAML)
  override val mediaTypes = new MediaTypeResolutionStage(ProfileNames.RAML)
  override val examples   = new ExamplesResolutionStage(ProfileNames.RAML)

  override def resolve[T <: BaseUnit](model: T): T = {
    withModel(model) { () =>
      commonSteps()
      step(parameters)
      step(mediaTypes)
      step(examples)
    }
  }

}
