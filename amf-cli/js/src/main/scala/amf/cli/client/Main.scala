package amf.cli.client

import amf.apicontract.client.scala.config.{AMFConfiguration, AsyncAPIConfiguration, WebAPIConfiguration}
import amf.cli.internal.commands._
import amf.client.environment.AsyncAPIConfiguration
import amf.core.internal.benchmark.ExecutionLog
import amf.core.internal.unsafe.PlatformSecrets

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

  private def enableTracing(cfg: ParserConfig) = if (cfg.trace) {
    println("Enabling tracing!")
    ExecutionLog.start()
  }

  def main(rawArgs: js.Array[String]): js.Promise[Any] = {
    val args = rawArgs.toArray
    CmdLineParser.parse(args) match {
      case Some(cfg) =>
        enableTracing(cfg)
        cfg.mode match {
          case Some(ParserConfig.REPL) =>
            println("REPL not supported in the JS client yet")
            failCommand()
            throw new Exception("Error executing AMF")
          case Some(ParserConfig.TRANSLATE) =>
            val f = runTranslate(cfg)
            f.failed.foreach(e => failPromise(e))
            f.toJSPromise
          case Some(ParserConfig.VALIDATE) =>
            val f = runValidate(cfg)
            f.failed.foreach(e => failPromise(e))
            f.toJSPromise
          case Some(ParserConfig.PARSE) =>
            val f = runParse(cfg)
            f.failed.foreach(e => failPromise(e))
            val composed = f.transform { r =>
              println("... composing...")
              ExecutionLog.finish().buildReport()
              r
            }
            composed.toJSPromise
          case Some(ParserConfig.PATCH) =>
            val f = runPatch(cfg)
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

  def runTranslate(config: ParserConfig): Future[Any] = amfConfig.map(TranslateCommand(platform).run(config, _))
  def runValidate(config: ParserConfig): Future[Any]  = amfConfig.map(ValidateCommand(platform).run(config, _))
  def runParse(config: ParserConfig): Future[Any]     = amfConfig.map(ParseCommand(platform).run(config, _))
  def runPatch(config: ParserConfig): Future[Any]     = amfConfig.map(PatchCommand(platform).run(config, _))

  private val amfConfig: Future[AMFConfiguration] =
    WebAPIConfiguration.WebAPI().merge(AsyncAPIConfiguration.Async20()).withCustomValidationsEnabled()
}
