package amf.cli.internal.commands

import amf.aml.client.scala.AMLConfiguration
import amf.aml.client.scala.model.document.DialectInstance
import amf.aml.internal.parse.plugin.AMLDialectInstanceParsingPlugin
import amf.apicontract.client.scala.AMFConfiguration
import amf.core.client.common.validation.ProfileName
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.remote.Platform

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

  def checkValidation(parserConfig: ParserConfig, model: BaseUnit, configuration: AMLConfiguration): Future[Unit] = {
    implicit val context: ExecutionContext = configuration.getExecutionContext
    val customProfileLoaded: Future[ProfileName] = if (parserConfig.customProfile.isDefined) {
      configuration.createClient().parseValidationProfile(parserConfig.customProfile.get) map (_.name)
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
              .getOrElse(parserConfig.profile)
          case _ =>
            parserConfig.profile
        }
      }
    }
    customProfileLoaded flatMap { profileName =>
      configuration.createClient().validate(model, profileName) map { report =>
        if (!report.conforms) {
          parserConfig.stderr.print(report.toString)
          parserConfig.proc.exit(ExitCodes.FailingValidation)
        }
      }
    }
  }
}

object TranslateCommand {
  def apply(platform: Platform) = new TranslateCommand(platform)
}
