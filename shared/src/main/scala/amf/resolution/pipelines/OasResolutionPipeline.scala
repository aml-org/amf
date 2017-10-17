package amf.resolution.pipelines
import amf.ProfileNames
import amf.document.BaseUnit
import amf.resolution.stages.ParametersNormalizationStage

class OasResolutionPipeline extends AmfResolutionPipeline {

  override val parameters = new ParametersNormalizationStage(ProfileNames.OAS)

  override def resolve(model: BaseUnit) = {
    withModel(model) { () =>
      commonSteps()
      step(parameters)
    }
  }

}
