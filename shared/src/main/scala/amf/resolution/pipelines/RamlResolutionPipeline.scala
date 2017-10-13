package amf.resolution.pipelines
import amf.ProfileNames
import amf.document.BaseUnit
import amf.resolution.stages.ParametersNormalizationStage

class RamlResolutionPipeline() extends AmfResolutionPipeline {

  override val parameters = new ParametersNormalizationStage(ProfileNames.RAML)

  override def resolve(model: BaseUnit): BaseUnit = {
    withModel(model) { () =>
      commonSteps()
      step(parameters)
    }
  }

}
