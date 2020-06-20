package amf.client.commands

import amf.ProfileName
import amf.core.client.{ExitCodes, ParserConfig}
import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.remote.Platform
import amf.core.services.RuntimeValidator
import amf.plugins.document.vocabularies.AMLPlugin
import amf.plugins.document.vocabularies.model.document.DialectInstance

import scala.concurrent.Future
import scala.util.{Failure, Success}

class TranslateCommand(override val platform: Platform) extends CommandHelper {

  val validationCommand = new ValidateCommand(platform)

  def run(config: ParserConfig): Future[Any] = {
    val res: Future[Any] = for {
      _         <- AMFInit()
      model     <- parseInput(config)
      _         <- checkValidation(config, model)
      model     <- resolve(config, model)
      generated <- generateOutput(config, model)
    } yield {
      generated
    }

    res.onComplete {
      case Failure(ex: Throwable) => {
        config.stderr.print(ex)
        config.proc.exit(ExitCodes.Exception)
      }
      case Success(other) => other
    }
    res
  }

  def checkValidation(config: ParserConfig, model: BaseUnit): Future[Unit] = {
    val customProfileLoaded: Future[ProfileName] = if (config.customProfile.isDefined) {
      RuntimeValidator.loadValidationProfile(config.customProfile.get, errorHandler = UnhandledErrorHandler) map {
        profileName =>
          profileName
      }
    } else {
      Future {
        model match {
          case dialectInstance: DialectInstance =>
            AMLPlugin().registry.dialectFor(dialectInstance) match {
              case Some(dialect) =>
                ProfileName(dialect.nameAndVersion())
              case _ =>
                config.profile
            }
          case _ =>
            config.profile
        }
      }
    }
    customProfileLoaded flatMap { profileName =>
      RuntimeValidator(model, profileName) map { report =>
        if (!report.conforms) {
          config.stderr.print(report.toString)
          config.proc.exit(ExitCodes.FailingValidation)
        }
      }
    }
  }
}

object TranslateCommand {
  def apply(platform: Platform) = new TranslateCommand(platform)
}
