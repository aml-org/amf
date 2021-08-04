package amf.cli.client

import amf.apicontract.client.scala.{AMFConfiguration, APIConfiguration, AsyncAPIConfiguration, WebAPIConfiguration}
import amf.cli.internal.commands._
import amf.core.client.scala.config.event.{AMFEventReportBuilder, TimedEventListener}
import amf.core.internal.unsafe.PlatformSecrets

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.JSConverters.JSRichFutureNonThenable
import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Main entry point for the application
  */
@JSExportTopLevel("Main")
@JSExportAll
object Main extends PlatformSecrets {

  private val reportBuilder               = AMFEventReportBuilder()
  private var amfConfig: AMFConfiguration = APIConfiguration.API()

  private def enableTracing(cfg: ParserConfig): AMFConfiguration =
    if (cfg.trace) {
      amfConfig.withEventListener(TimedEventListener(() => js.Date.now().toLong, event => reportBuilder.add(event)))
    } else amfConfig

  def main(rawArgs: js.Array[String]): js.Promise[Any] = {
    val args = rawArgs.toArray
    CmdLineParser.parse(args) match {
      case Some(cfg) =>
        val amfConfig = enableTracing(cfg)
        cfg.mode match {
          case Some(ParserConfig.REPL) =>
            println("REPL not supported in the JS client yet")
            failCommand()
            throw new Exception("Error executing AMF")
          case Some(ParserConfig.TRANSLATE) =>
            val f = runTranslate(cfg, amfConfig)
            f.failed.foreach(e => failPromise(e))
            f.toJSPromise
          case Some(ParserConfig.VALIDATE) =>
            val f = runValidate(cfg, amfConfig)
            f.failed.foreach(e => failPromise(e))
            f.toJSPromise
          case Some(ParserConfig.PARSE) =>
            val f = runParse(cfg, amfConfig)
            f.failed.foreach(e => failPromise(e))
            val composed = f.transform { r =>
              println("... composing...")
              reportBuilder.build().print()
              reportBuilder.reset()
              r
            }
            composed.toJSPromise
          case Some(ParserConfig.PATCH) =>
            val f = runPatch(cfg, amfConfig)
            f.failed.foreach(e => failPromise(e))
            f.toJSPromise
          case _ =>
            failCommand()
            throw new Exception("Error executing AMF")
        }
      case _ =>
        js.Dynamic.global.process.exit(ExitCodes.WrongInvocation)
        throw new Exception("Error executing AMF")
    }
  }

  def failCommand(): Unit = {
    System.err.println("Wrong command")
    js.Dynamic.global.process.exit(ExitCodes.WrongInvocation)
  }
  def failPromise(e: Any): Unit = {
    System.err.println("Exception")
    System.err.println(e)
    js.Dynamic.global.process.exit(ExitCodes.WrongInvocation)
  }

  def runTranslate(config: ParserConfig, amfConfig: AMFConfiguration): Future[Any] =
    TranslateCommand(platform).run(config, amfConfig)
  def runValidate(config: ParserConfig, amfConfig: AMFConfiguration): Future[Any] =
    ValidateCommand(platform).run(config, amfConfig)
  def runParse(config: ParserConfig, amfConfig: AMFConfiguration): Future[Any] =
    ParseCommand(platform).run(config, amfConfig)
  def runPatch(config: ParserConfig, amfConfig: AMFConfiguration): Future[Any] =
    PatchCommand(platform).run(config, amfConfig)
}
