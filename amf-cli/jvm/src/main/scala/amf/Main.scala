package amf

import amf.apicontract.client.scala.{AMFConfiguration, AsyncAPIConfiguration, WebAPIConfiguration}
import amf.cli.internal.commands.{CmdLineParser, ExitCodes, ParseCommand, ParserConfig, PatchCommand, TranslateCommand, ValidateCommand}
import amf.core.internal.unsafe.PlatformSecrets

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.{existentials, postfixOps}
import scala.util.Failure

/**
  * Main entry point for the application
  */
object Main extends PlatformSecrets {

  def enableTracing(cfg: ParserConfig) = {
    if (cfg.trace) {
      System.err.println("Tracing enabled")
      ExecutionLog.start()
    }
  }

  def main(args: Array[String]): Unit = {
    CmdLineParser.parse(args) match {
      case Some(cfg) =>
        enableTracing(cfg)
        cfg.mode match {
          case Some(ParserConfig.TRANSLATE) => Await.result(runTranslate(cfg), 1 day)
          case Some(ParserConfig.VALIDATE)  => Await.result(runValidate(cfg), 1 day)
          case Some(ParserConfig.PARSE) => {
            val f = runParse(cfg)
            val ff = f.transform { r =>
              r match {
                case Failure(exception) =>
                  exception.printStackTrace()
                case _                  => // ignore
              }
              if (cfg.trace) {
                println("\n\n\n\n")
                ExecutionLog.finish().buildReport()
              }
              r
            }
            Await.ready(ff, 1 day)
          }
          case Some(ParserConfig.PATCH) => Await.ready(runPatch(cfg), 1 day)
          case _                        => failCommand()
        }
      case _ => System.exit(ExitCodes.WrongInvocation)
    }
    System.exit(ExitCodes.Success)
  }

  def failCommand(): Unit = {
    System.err.println("Wrong command")
    System.exit(ExitCodes.WrongInvocation)
  }
  def runTranslate(config: ParserConfig): Future[Any] = amfConfig.flatMap(TranslateCommand(platform).run(config, _))
  def runValidate(config: ParserConfig): Future[Any]  = amfConfig.flatMap(ValidateCommand(platform).run(config, _))
  def runParse(config: ParserConfig): Future[Any]     = amfConfig.flatMap(ParseCommand(platform).run(config, _))
  def runPatch(config: ParserConfig): Future[Any]     = amfConfig.flatMap(PatchCommand(platform).run(config, _))

  private val amfConfig: Future[AMFConfiguration] =
    Future(WebAPIConfiguration.WebAPI().merge(AsyncAPIConfiguration.Async20()))
}
