package amf.client.commands

import amf.client.{ExitCodes, ParserConfig}
import amf.remote.Platform
import amf.validation.ValidationProfileNames

import scala.concurrent.Future
import scala.util.{Failure, Success}

class ParseCommand(override val platform: Platform) extends TranslateCommand(platform) {

  override def run(origConfig: ParserConfig): Future[Any] = {
    val config = origConfig.copy(outputFormat = Some(ValidationProfileNames.AMF))
    val res = for {
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
