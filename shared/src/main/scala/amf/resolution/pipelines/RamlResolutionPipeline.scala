package amf.resolution.pipelines
import amf.ProfileNames
import amf.document.BaseUnit
import amf.resolution.stages.{MediaTypeResolutionStage, ParametersNormalizationStage}

class RamlResolutionPipeline() extends AmfResolutionPipeline {

  override val parameters = new ParametersNormalizationStage(ProfileNames.RAML)
  override val mediaTypes = new MediaTypeResolutionStage(ProfileNames.RAML)

  override def resolve(model: BaseUnit): BaseUnit = {
    withModel(model) { () =>
      commonSteps()
      step(parameters)
      step(mediaTypes)
    }
  }

}
