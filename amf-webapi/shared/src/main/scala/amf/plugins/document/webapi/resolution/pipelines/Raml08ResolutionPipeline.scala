package amf.plugins.document.webapi.resolution.pipelines

import amf.ProfileNames
import amf.core.model.document.BaseUnit
import amf.plugins.document.webapi.resolution.stages.{ExtendsResolutionStage, ExtensionsResolutionStage}
import amf.plugins.domain.webapi.resolution.stages.{
  ExamplesResolutionStage,
  MediaTypeResolutionStage,
  ParametersNormalizationStage
}

class Raml08ResolutionPipeline extends AmfResolutionPipeline {

  override val parameters = new ParametersNormalizationStage(ProfileNames.RAML08)
  override val mediaTypes = new MediaTypeResolutionStage(ProfileNames.RAML08)
  override val examples   = new ExamplesResolutionStage(ProfileNames.RAML08)
  override val extensions = new ExtensionsResolutionStage(ProfileNames.RAML08)

  override def resolve[T <: BaseUnit](model: T): T = {
    withModel(model) { () =>
      commonSteps()
      step(parameters)
      step(mediaTypes)
      step(examples)
      step(cleanRefs)
      step(cleanDecls)
    }
  }

}
