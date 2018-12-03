package amf.core.resolution.pipelines

import amf.ProfileName
import amf.core.benchmark.ExecutionLog
import amf.core.model.document.BaseUnit
import amf.core.parser.ErrorHandler
import amf.core.resolution.stages.ResolutionStage

import scala.scalajs.js.annotation.JSExportAll

abstract class ResolutionPipeline(val eh: ErrorHandler) {

//  val model: T
  def profileName: ProfileName
  implicit val errorHandler: ErrorHandler = eh
  // todo: replace for actual context when remove static error collection
  // default should be unhandled? or we need to provide a result at resolution??
//  implicit val errorHandler: ErrorHandler = {
//
//    new ErrorHandler {
//      override val parserCount: Int = {
//        // this can get not set if the model has been created manually without parsing
//        model.parserRun match {
//          case Some(run) => run
//          case None =>
//            model.parserRun = Some(AMFCompilerRunCount.nextRun())
//            model.parserRun.get
//        }
//      }
//      override val currentFile: String = model.location().getOrElse(model.id)
//    }
//  }
//
//  def profileName: ProfileName
  val steps: Seq[ResolutionStage]

  final def resolve[T <: BaseUnit](model: T): T = {
    ExecutionLog.log(s"${this.getClass.getName}#resolve: resolving ${model.location().getOrElse("")}")
    var m = model
    steps.foreach { s =>
      m = step(m, s)
    }
    ExecutionLog.log(s"${this.getClass.getName}#resolve: resolved model ${m.location().getOrElse("")}")
    m
  }

  protected def step[T <: BaseUnit](unit: T, stage: ResolutionStage): T = {
    ExecutionLog.log(s"ResolutionPipeline#step: applying resolution stage ${stage.getClass.getName}")
    val resolved = stage.resolve(unit)
    ExecutionLog.log(s"ResolutionPipeline#step: finished applying stage ${stage.getClass.getName}")
    resolved
  }
}

@JSExportAll
object ResolutionPipeline {
  val DEFAULT_PIPELINE       = "default"
  val EDITING_PIPELINE       = "editing"
  val COMPATIBILITY_PIPELINE = "compatibility"
}
