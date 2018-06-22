package amf.plugins.document.vocabularies.resolution.pipelines

import amf.ProfileNames
import amf.core.model.document.BaseUnit
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages.{CleanReferencesStage, DeclarationsRemovalStage, ReferenceResolutionStage}
import amf.plugins.document.vocabularies.resolution.stages.DialectInstanceReferencesResolutionStage

class DialectInstanceResolutionPipeline extends ResolutionPipeline {

  val references = new DialectInstanceReferencesResolutionStage(ProfileNames.AMF)
  val cleanRefs  = new CleanReferencesStage(ProfileNames.AMF)
  val cleanDecls = new DeclarationsRemovalStage(ProfileNames.AMF)

  override def resolve[T <: BaseUnit](model: T): T = {
    withModel(model) { () =>
      step(references)
      step(cleanRefs)
      step(cleanDecls)
    }
  }
}
