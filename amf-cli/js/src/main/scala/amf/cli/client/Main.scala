package amf.cli.client

import amf.apicontract.client.scala.{AMFConfiguration, AsyncAPIConfiguration, WebAPIConfiguration}
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

  private val reportBuilder = AMFEventReportBuilder()
  private var amfConfig: Future[AMFConfiguration] =
    WebAPIConfiguration.WebAPI().merge(AsyncAPIConfiguration.Async20()).withCustomValidationsEnabled()

  private def enableTracing(cfg: ParserConfig): Future[AMFConfiguration] =
    if (cfg.trace) {
      amfConfig.map(config =>
        config.withEventListener(TimedEventListener(() => js.Date.now().toLong, event => reportBuilder.add(event))))
    } else amfConfig

  def main(rawArgs: js.Array[String]): js.Promise[Any] = {
    val args = rawArgs.toArray
    CmdLineParser.parse(args) match {
      case Some(cfg) =>
        val futureConfig = enableTracing(cfg)
        cfg.mode match {
          case Some(ParserConfig.REPL) =>
            println("REPL not supported in the JS client yet")
            failCommand()
            throw new Exception("Error executing AMF")
          case Some(ParserConfig.TRANSLATE) =>
            val f = runTranslate(cfg, futureConfig)
            f.failed.foreach(e => failPromise(e))
            f.toJSPromise
          case Some(ParserConfig.VALIDATE) =>
            val f = runValidate(cfg, futureConfig)
            f.failed.foreach(e => failPromise(e))
            f.toJSPromise
          case Some(ParserConfig.PARSE) =>
            val f = runParse(cfg, futureConfig)
            f.failed.foreach(e => failPromise(e))
            val composed = f.transform { r =>
              println("... composing...")
              reportBuilder.build().print()
              reportBuilder.reset()
              r
            }
            composed.toJSPromise
          case Some(ParserConfig.PATCH) =>
            val f = runPatch(cfg, futureConfig)
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

  def runTranslate(config: ParserConfig, futureConfig: Future[AMFConfiguration]): Future[Any] =
    futureConfig.map(TranslateCommand(platform).run(config, _))
  def runValidate(config: ParserConfig, futureConfig: Future[AMFConfiguration]): Future[Any] =
    futureConfig.map(ValidateCommand(platform).run(config, _))
  def runParse(config: ParserConfig, futureConfig: Future[AMFConfiguration]): Future[Any] =
    futureConfig.map(ParseCommand(platform).run(config, _))
  def runPatch(config: ParserConfig, futureConfig: Future[AMFConfiguration]): Future[Any] =
    futureConfig.map(PatchCommand(platform).run(config, _))
}
