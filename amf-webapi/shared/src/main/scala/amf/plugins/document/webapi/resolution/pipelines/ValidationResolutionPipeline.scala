package amf.plugins.document.webapi.resolution.pipelines

import amf.core.benchmark.ExecutionLog
import amf.core.model.document.BaseUnit
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages.ReferenceResolutionStage
import amf.plugins.document.webapi.resolution.stages.ExtensionsResolutionStage
import amf.plugins.domain.shapes.resolution.stages.ShapeNormalizationStage

class ValidationResolutionPipeline(profile: String) extends ResolutionPipeline {

  val references = new ReferenceResolutionStage(profile, keepEditingInfo = false)
  val extensions = new ExtensionsResolutionStage(profile, keepEditingInfo = false)
  val shapes     = new ShapeNormalizationStage(profile, keepEditingInfo = false)

  override def resolve[T <: BaseUnit](model: T): T = {
    ExecutionLog.log(s"ValidationResolutionPipeline#resolve: resolving ${model.location}")
    withModel(model) { () =>
      step(references)
      step(extensions)
      step(shapes)
      ExecutionLog.log(s"ValidationResolutionPipeline#resolve: resolution finished ${model.location}")
    }
  }

}
