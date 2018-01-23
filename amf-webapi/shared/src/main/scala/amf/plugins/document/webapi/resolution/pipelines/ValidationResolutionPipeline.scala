package amf.plugins.document.webapi.resolution.pipelines

import amf.core.model.document.BaseUnit
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages.ReferenceResolutionStage
import amf.plugins.document.webapi.resolution.stages.ExtensionsResolutionStage
import amf.plugins.domain.shapes.resolution.stages.ShapeNormalizationStage

class ValidationResolutionPipeline(profile: String) extends ResolutionPipeline {

  val references = new ReferenceResolutionStage(profile)
  val extensions = new ExtensionsResolutionStage(profile)
  val shapes     = new ShapeNormalizationStage(profile)

  override def resolve[T <: BaseUnit](model: T): T = {
    withModel(model) { () =>
      step(references)
      step(extensions)
      step(shapes)
    }
  }

}
