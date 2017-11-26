package amf.core.resolution.pipelines

import amf.ProfileNames
import amf.core.model.document.BaseUnit
import amf.core.resolution.stages.ReferenceResolutionStage

class BasicResolutionPipeline extends ResolutionPipeline{

  val references = new ReferenceResolutionStage(ProfileNames.AMF)

  override def resolve[T <: BaseUnit](model: T): T = {
    withModel(model) { () =>
      step(references)
    }
  }

}
