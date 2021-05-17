package amf.client.commands

import amf.client.remod.AMFGraphConfiguration
import amf.core.client.{ExitCodes, ParserConfig}
import amf.core.remote.Platform

import scala.concurrent.Future
import scala.util.{Failure, Success}

class ParseCommand(override val platform: Platform, override val configuration: AMFGraphConfiguration)
    extends TranslateCommand(platform, configuration) {

  override def run(origConfig: ParserConfig): Future[Any] = {
    val config = origConfig.copy(outputFormat = Some("AMF Graph"), outputMediaType = Some("application/ld+json"))
    val res = for {
      _         <- AMFInit()
      _         <- processDialects(config)
      model     <- parseInput(config)
      _         <- checkValidation(config, model)
      model     <- resolve(config, model)
      generated <- generateOutput(config, model)
    } yield {
      generated
    }

    res.onComplete {

      case Failure(ex: Throwable) =>
        config.stderr.print(ex)
        config.proc.exit(ExitCodes.Exception)
      case Success(other) => other
    }

    res
  }

}

object ParseCommand {
  def apply(platform: Platform, configuration: AMFGraphConfiguration) = new ParseCommand(platform, configuration)
}
