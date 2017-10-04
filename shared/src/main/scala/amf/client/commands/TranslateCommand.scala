package amf.client.commands

import amf.client.{ExitCodes, ParserConfig}
import amf.document.BaseUnit
import amf.remote._
import amf.validation.Validation

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

class TranslateCommand(override val platform: Platform) extends CommandHelper {

  val validationCommand = new ValidateCommand(platform)
  var validation: Option[Validation] = None

  def run(config: ParserConfig): Future[Any] = {
    val res = for {
      _          <- setupValidationTranslate(config)
      model      <- parseInput(config)
      _          <- checkValidation(config, model)
      generated  <- generateOutput(config, model)
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

  def setupValidationTranslate(config:ParserConfig): Future[Unit] = {
    if (config.validate) {
      setupValidation(config).map { validation =>
        this.validation = Some(validation)
      }
    } else {
      Promise().success().future
    }
  }

  def checkValidation(config: ParserConfig, model: BaseUnit): Future[Unit] = {
    Future {
      if (validation.isDefined) {
        val profile = config.customProfile match {
          case Some(_) => validation.get.profile.get.name
          case None    => config.validationProfile
        }
        validation.get.validate(model, profile, config.validationProfile) map { report =>
          if (!report.conforms) {
            System.err.println(report)
            platform.exit(ExitCodes.FailingValidation)
          }
        }
      }
    }
  }
}

object TranslateCommand {
  def apply(platform: Platform) = new TranslateCommand(platform)
}

