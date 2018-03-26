package amf.core.resolution.pipelines

import amf.core.benchmark.ExecutionLog
import amf.core.model.document.BaseUnit
import amf.core.resolution.stages.ResolutionStage

abstract class ResolutionPipeline {

  var model: Option[BaseUnit] = None

  def resolve[T <: BaseUnit](model: T): T

  protected def step(stage: ResolutionStage): Unit = {
    ExecutionLog.log(s"ResolutionPipeline#step: applying resolution stage ${stage.getClass.getName}")
    model = Some(stage.resolve(model.get))
    ExecutionLog.log(s"ResolutionPipeline#step: finished applying stage ${stage.getClass.getName}")
  }

  protected def withModel[T <: BaseUnit](unit: T)(block: () => Unit): T = {
    model = Some(unit)
    block()
    model.get.asInstanceOf[T]
  }
}

object ResolutionPipeline {
  val DEFAULT_PIPELINE = "default"
  val EDITING_PIPELINE = "editing"
}