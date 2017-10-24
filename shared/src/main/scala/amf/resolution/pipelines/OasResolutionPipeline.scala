package amf.resolution.pipelines
import amf.ProfileNames
import amf.document.BaseUnit
import amf.resolution.stages.{MediaTypeResolutionStage, ParametersNormalizationStage}

class OasResolutionPipeline extends AmfResolutionPipeline {

  override val parameters = new ParametersNormalizationStage(ProfileNames.OAS)
  override val mediaTypes = new MediaTypeResolutionStage(ProfileNames.OAS)

  override def resolve(model: BaseUnit): BaseUnit = {
    withModel(model) { () =>
      commonSteps()
      step(parameters)
      step(mediaTypes)
    }
  }

}
