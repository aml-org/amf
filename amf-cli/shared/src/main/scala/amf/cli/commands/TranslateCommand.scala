package amf.cli.commands

import amf.ProfileName
import amf.client.environment.{AMFConfiguration, AMLConfiguration}
import amf.client.remod.parsing.AMLDialectInstanceParsingPlugin
import amf.core.client.{ExitCodes, ParserConfig}
import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.remote.Platform
import amf.core.services.RuntimeValidator
import amf.plugins.document.vocabularies.model.document.DialectInstance

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class TranslateCommand(override val platform: Platform) extends CommandHelper {

  def run(parserConfig: ParserConfig, configuration: AMFConfiguration): Future[Any] = {
    implicit val context: ExecutionContext = configuration.getExecutionContext
    val res: Future[Any] = for {
      _         <- AMFInit(configuration)
      model     <- parseInput(parserConfig, configuration)
      _         <- checkValidation(parserConfig, model, configuration)
      model     <- resolve(parserConfig, model, configuration)
      generated <- generateOutput(parserConfig, model, configuration)
    } yield {
      generated
    }

    res.onComplete {
      case Failure(ex: Throwable) => {
        parserConfig.stderr.print(ex)
        parserConfig.proc.exit(ExitCodes.Exception)
      }
      case Success(other) => other
    }
    res
  }

  def checkValidation(config: ParserConfig, model: BaseUnit, configuration: AMLConfiguration): Future[Unit] = {
    implicit val context: ExecutionContext = configuration.getExecutionContext
    val customProfileLoaded: Future[ProfileName] = if (config.customProfile.isDefined) {
      RuntimeValidator.loadValidationProfile(config.customProfile.get, errorHandler = UnhandledErrorHandler) map {
        profileName =>
          profileName
      }
    } else {
      Future {
        model match {
          case dialectInstance: DialectInstance =>
            configuration.registry.plugins.parsePlugins
              .collect {
                case plugin: AMLDialectInstanceParsingPlugin => plugin.dialect
              }
              .find(dialect => dialectInstance.definedBy().value() == dialect.id)
              .map(dialect => ProfileName(dialect.nameAndVersion()))
              .getOrElse(config.profile)
          case _ =>
            config.profile
        }
      }
    }
    customProfileLoaded flatMap { profileName =>
      configuration.createClient().validate(model, profileName) map { report =>
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
