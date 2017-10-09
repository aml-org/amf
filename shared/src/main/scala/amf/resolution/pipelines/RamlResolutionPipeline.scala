package amf.resolution.pipelines
import amf.document.BaseUnit

class RamlResolutionPipeline() extends AmfResolutionPipeline {

  override def resolve(model: BaseUnit): BaseUnit = {
    withModel(model) { () =>
      commonSteps()
    }
  }

}
