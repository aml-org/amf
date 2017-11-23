package amf.client.commands

import amf.client.{ExitCodes, ParserConfig}
import amf.framework.model.document.BaseUnit
import amf.remote._
import amf.validation.Validation

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

class TranslateCommand(override val platform: Platform) extends CommandHelper {

  val validationCommand              = new ValidateCommand(platform)
  var validation: Validation         = new Validation(platform)

  def run(config: ParserConfig): Future[Any] = {
    val res = for {
      _         <- processDialects(config)
      _         <- setupValidationTranslate(config)
      model     <- parseInput(config, validation)
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

  def setupValidationTranslate(config: ParserConfig): Future[Validation] = {
    if (config.validate) {
      setupValidation(config).map { validation =>
        this.validation = validation
        validation
      }
    } else {
      validation.validator.enabled = false
      Promise().success(validation).future
    }
  }

  def checkValidation(config: ParserConfig, model: BaseUnit): Future[Unit] = {
    if (validation.enabled) {
      val profile = config.customProfile match {
        case Some(_) => validation.profile.get.name
        case None    => config.validationProfile
      }
      validation.validate(model, profile, config.validationProfile) map { report =>
        if (!report.conforms) {
          System.err.println(report)
          platform.exit(ExitCodes.FailingValidation)
        }
      }
    } else {
      Promise().success().future
    }
  }
}

object TranslateCommand {
  def apply(platform: Platform) = new TranslateCommand(platform)
}
