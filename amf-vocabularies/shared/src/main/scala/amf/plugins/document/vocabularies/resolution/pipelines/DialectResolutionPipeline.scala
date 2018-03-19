package amf.plugins.document.vocabularies.resolution.pipelines

import amf.ProfileNames
import amf.core.model.document.BaseUnit
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.plugins.document.vocabularies.resolution.stages.DialectReferencesResolutionStage

class DialectResolutionPipeline extends ResolutionPipeline {

  val references = new DialectReferencesResolutionStage(ProfileNames.AMF)

  override def resolve[T <: BaseUnit](model: T): T = {
    withModel(model) { () =>
      step(references)
    }
  }
}
