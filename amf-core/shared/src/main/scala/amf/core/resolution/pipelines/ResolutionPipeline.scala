package amf.core.resolution.pipelines

import amf.core.model.document.BaseUnit
import amf.core.resolution.stages.ResolutionStage

abstract class ResolutionPipeline {

  var model: Option[BaseUnit] = None

  def resolve[T <: BaseUnit](model: T): T

  protected def step(stage: ResolutionStage): Unit = {
    model = Some(stage.resolve(model.get))
  }

  protected def withModel[T <: BaseUnit](unit: T)(block: () => Unit): T = {
    model = Some(unit)
    block()
    model.get.asInstanceOf[T]
  }
}