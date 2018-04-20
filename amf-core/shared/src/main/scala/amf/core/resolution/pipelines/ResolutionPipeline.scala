package amf.core.resolution.pipelines

import amf.core.AMFCompilerRunCount
import amf.core.benchmark.ExecutionLog
import amf.core.model.document.BaseUnit
import amf.core.parser.ErrorHandler
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

  protected def errorHandlerForModel(model: BaseUnit): ErrorHandler = {
    new ErrorHandler {
      override val parserCount: Int = {
        // this can get not set if the model has been created manually without parsing
        model.parserRun match {
          case Some(run) => run
          case None      =>
            model.parserRun = Some(AMFCompilerRunCount.nextRun())
            model.parserRun.get
        }
      }
      override val currentFile: String = model.location
    }
  }
}

object ResolutionPipeline {
  val DEFAULT_PIPELINE = "default"
  val EDITING_PIPELINE = "editing"
}