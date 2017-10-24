package amf.client.commands

import amf.ProfileNames
import amf.client.{ExitCodes, ParserConfig}
import amf.remote.Platform

import scala.concurrent.Future
import scala.util.{Failure, Success}

class ParseCommand(override val platform: Platform) extends TranslateCommand(platform) {

  override def run(origConfig: ParserConfig): Future[Any] = {
    val config = origConfig.copy(outputFormat = Some(ProfileNames.AMF))
    val res = for {
      _         <- processDialects(config)
      _         <- setupValidationTranslate(config)
      model     <- parseInput(config)
      _         <- checkValidation(config, model)
      generated <- generateOutput(config, model)
    } yield {
      generated
    }

    res.onComplete {
      case Failure(ex) => {
        System.err.println(ex)
        platform.exit(ExitCodes.Exception)
      }
      case Success(other) => other
    }

    res
  }

}

object ParseCommand {
  def apply(platform: Platform) = new ParseCommand(platform)
}
