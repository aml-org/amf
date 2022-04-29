package amf.cli.client

import amf.aml.client.scala.AMLConfiguration
import amf.apicontract.client.scala.{
  AMFConfiguration,
  APIConfiguration,
  AsyncAPIConfiguration,
  OASConfiguration,
  RAMLConfiguration,
  WebAPIConfiguration
}
import amf.cli.internal.commands._
import amf.core.client.common.remote.Content
import amf.core.client.common.validation.{
  AmfProfile,
  AmlProfile,
  GrpcProfile,
  Oas20Profile,
  Oas30Profile,
  Raml08Profile,
  Raml10Profile
}
import amf.core.client.platform.resource.FileResourceLoader
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.config.event.{AMFEventReportBuilder, TimedEventListener}
import amf.core.client.scala.resource.ResourceLoader
import amf.core.internal.remote.Grpc
import amf.core.internal.unsafe.PlatformSecrets
import amf.graphql.client.scala.GraphQLConfiguration
import amf.grpc.client.scala.GRPCConfiguration

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

/** Main entry point for the application
  */
object Main extends PlatformSecrets {

  private val reportBuilder               = AMFEventReportBuilder()
  private var amfConfig: AMLConfiguration = APIConfiguration.API()

  private def buildAMFConfiguration(cfg: ParserConfig): AMLConfiguration = {
    val effectiveCfg = cfg.inputFormat match {
      case Some(s) if s == Raml10Profile.profile => RAMLConfiguration.RAML10()
      case Some(s) if s == Raml08Profile.profile => RAMLConfiguration.RAML08()
      case Some(s) if s == Oas20Profile.profile  => OASConfiguration.OAS20()
      case Some(s) if s == Oas30Profile.profile  => OASConfiguration.OAS30()
      case Some(s) if s == AmfProfile.profile    => APIConfiguration.API()
      case Some(s) if s == GrpcProfile.profile   => GRPCConfiguration.GRPC()
      case Some(s) if s == "GRAPHQL"             => GraphQLConfiguration.GraphQL()
      case Some(s) if s == AmlProfile.profile    => AMLConfiguration.predefined()
    }
    effectiveCfg.withResourceLoader(new ResourceLoader() {
      val wrapped = new FileResourceLoader()
      override def fetch(resource: String): Future[Content] = {
        val fetched = wrapped.baseFetchFile(resource)
        Future { fetched.copy(mime = cfg.inputMediaType) }
      }

      override def accepts(resource: String): Boolean = true
    })
  }

  private def enableTracing(cfg: ParserConfig, config: AMLConfiguration) = {
    if (cfg.trace) {
      System.err.println("Tracing enabled")
      amfConfig = config.withEventListener(
        TimedEventListener(() => Instant.now().toEpochMilli, event => reportBuilder.add(event))
      )
    }
  }

  def main(args: Array[String]): Unit = {

    CmdLineParser.parse(args) match {
      case Some(cfg) =>
        amfConfig = buildAMFConfiguration(cfg)
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
