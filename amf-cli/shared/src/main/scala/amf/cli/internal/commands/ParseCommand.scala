package amf.cli.internal.commands

import amf.aml.client.scala.AMLConfiguration
import amf.apicontract.client.scala.AMFConfiguration
import amf.core.internal.remote.Mimes._
import amf.core.internal.remote.{Mimes, Platform}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class ParseCommand(override val platform: Platform) extends TranslateCommand(platform) {

  override def run(origConfig: ParserConfig, configuration: AMLConfiguration): Future[Any] = {
    implicit val ec: ExecutionContext = configuration.getExecutionContext
    val parserConfig = origConfig.copy(outputFormat = Some("AMF Graph"), outputMediaType = Some(`application/ld+json`))
    val res = for {
      newConf         <- processDialects(parserConfig, configuration)
      (model, specId) <- parseInput(parserConfig, newConf)
      _               <- checkValidation(parserConfig, model, specId, configuration)
      model           <- resolve(parserConfig, model, specId, configuration)
      generated       <- generateOutput(parserConfig, model, configuration)
    } yield {
      generated
    }

    res.onComplete {

      case Failure(ex: Throwable) =>
        parserConfig.stderr.print(ex)
        parserConfig.proc.exit(ExitCodes.Exception)
      case Success(other) => other
    }

    res
  }

}

object ParseCommand {
  def apply(platform: Platform) = new ParseCommand(platform)
}
