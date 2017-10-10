package amf.resolution.pipelines
import amf.document.BaseUnit

class OasResolutionPipeline extends AmfResolutionPipeline {

  override def resolve(model: BaseUnit) = {
    withModel(model) { () =>
      commonSteps()
    }
  }

}
