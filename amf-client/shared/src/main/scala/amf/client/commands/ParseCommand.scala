package amf.client.commands

import amf.core.client.{ExitCodes, ParserConfig}
import amf.core.remote.Platform

import scala.concurrent.Future
import scala.util.{Failure, Success}

class ParseCommand(override val platform: Platform) extends TranslateCommand(platform) {

  override def run(origConfig: ParserConfig): Future[Any] = {
    val config = origConfig.copy(outputFormat = Some("AMF Graph"), outputMediaType = Some("application/ld+json"))
    val res = for {
      _         <- AMFInit()
      _         <- processDialects(config)
      model     <- parseInput(config)
      _         <- if (config.validate) { checkValidation(config, model) } else Future(model)
      model     <- resolve(config, model)
      generated <- generateOutput(config, model)
    } yield {
      generated
    }

    res.onComplete {

      case Failure(ex: Throwable) =>
        config.stderr.print(ex)
        config.proc.exit(ExitCodes.Exception)
      case Success(other) =>
        other
    }

    res
  }

}

object ParseCommand {
  def apply(platform: Platform) = new ParseCommand(platform)
}
