package amf.core.benchmark

import java.util.Date

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

case class Log(stage: String, time: Long)

case class Execution(startTime: Long, endTime: Long, logs: Seq[Log]) {
  def log(stage: String, time: Long) = {
    copy(logs = logs ++ Seq(Log(stage, time)), endTime = time)
  }
  def finish(): Execution = copy(endTime = new Date().getTime)
}

@JSExportAll
@JSExportTopLevel("ExecutionLog")
object ExecutionLog {
  var executions: Seq[Execution] = Nil
  var current: Option[Execution] = None

  def log(stage: String): ExecutionLog.type = {
    current.foreach { execution =>
      val now = new Date().getTime
      current = Some(execution.log(stage, now))
    }
    this
  }

  def start(): ExecutionLog.type = {
    current.foreach { execution =>
      executions ++= Seq(execution.finish())
    }
    val now = new Date().getTime
    current = Some(Execution(now, now, Nil))

    this
  }

  def finish(): ExecutionLog.type = {
    current.foreach { execution =>
      executions ++= Seq(execution.finish())
    }
    current = None

    this
  }

  def buildReport() = {
    executions.zipWithIndex.foreach { case (execution, i) =>
      var prev = execution.startTime
      println(s"---- Run $i (${execution.endTime - execution.startTime} ms) ----\n")
      execution.logs.foreach { log =>
        println(s"   (${log.time - prev} ms) ${log.stage}")
        prev = log.time
      }
      println(s"   (${execution.endTime - prev} ms) Finished")
      println("\n\n\n")
    }
  }
}
