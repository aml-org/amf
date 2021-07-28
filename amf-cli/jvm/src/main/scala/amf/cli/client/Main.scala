package amf.cli.client

import amf.apicontract.client.scala.{AMFConfiguration, APIConfiguration, AsyncAPIConfiguration, WebAPIConfiguration}
import amf.cli.internal.commands._
import amf.core.client.scala.config.event.{AMFEventReportBuilder, TimedEventListener}
import amf.core.internal.remote.Grpc
import amf.core.internal.unsafe.PlatformSecrets
import amf.grpc.client.scala.GRPCConfiguration

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

/**
  * Main entry point for the application
  */
object Main extends PlatformSecrets {

  private val reportBuilder               = AMFEventReportBuilder()
  private var amfConfig: AMFConfiguration = APIConfiguration.API()

  private def enableTracing(cfg: ParserConfig, config: AMFConfiguration) = {
    if (cfg.trace) {
      System.err.println("Tracing enabled")
      amfConfig = config.withEventListener(
        TimedEventListener(() => Instant.now().toEpochMilli, event => reportBuilder.add(event)))
    }
  }

  def main(args: Array[String]): Unit = {

    CmdLineParser.parse(args) match {
      case Some(cfg) =>
        enableTracing(cfg, amfConfig)
        cfg.mode match {
          case Some(ParserConfig.TRANSLATE) => Await.result(runTranslate(cfg), 1 day)
          case Some(ParserConfig.VALIDATE)  => Await.result(runValidate(cfg), 1 day)
          case Some(ParserConfig.PARSE) =>
            val f = runParse(cfg)
            val ff = f.transform { r =>
              if (cfg.trace) {
                println("\n\n\n\n")
                reportBuilder.build().print()
                reportBuilder.reset()
              }
              r
            }
            Await.ready(ff, 1 day)
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
  def runTranslate(config: ParserConfig): Future[Any] = TranslateCommand(platform).run(config, amfConfig)
  def runValidate(config: ParserConfig): Future[Any]  = ValidateCommand(platform).run(config, amfConfig)
  def runParse(config: ParserConfig): Future[Any]     = ParseCommand(platform).run(config, amfConfig)
  def runPatch(config: ParserConfig): Future[Any]     = PatchCommand(platform).run(config, amfConfig)
}
