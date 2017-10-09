package amf.resolution.pipelines

import amf.ProfileNames
import amf.document.BaseUnit
import amf.resolution.stages.{ReferenceResolutionStage, ShapeNormalizationStage}

class AmfResolutionPipeline extends ResolutionPipeline {
  val references = new ReferenceResolutionStage(ProfileNames.AMF)
  val shapes     = new ShapeNormalizationStage(ProfileNames.AMF)

  override def resolve(model: BaseUnit): BaseUnit = {
    withModel(model) { () =>
      commonSteps()
    }
  }

  protected def commonSteps() = {
    step(references)
    step(shapes)
  }
}
