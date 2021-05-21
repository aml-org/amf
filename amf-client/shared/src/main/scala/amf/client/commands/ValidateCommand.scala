package amf.client.commands

import amf.ProfileName
import amf.client.environment.AMLConfiguration
import amf.client.remod.AMFGraphConfiguration
import amf.client.remod.amfcore.plugins.validate.ValidationConfiguration
import amf.core.client.{ExitCodes, ParserConfig}
import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.remote.Platform
import amf.core.services.RuntimeValidator
import amf.core.validation.AMFValidationReport
import amf.plugins.document.vocabularies.AMLPlugin
import amf.plugins.document.vocabularies.model.document.DialectInstance
import amf.plugins.features.validation.emitters.ValidationReportJSONLDEmitter

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class ValidateCommand(override val platform: Platform) extends CommandHelper {

  def run(config: ParserConfig, configuration: AMLConfiguration): Future[Any] = {
    implicit val context: ExecutionContext = configuration.getExecutionContext
    val res = for {
      _        <- AMFInit(configuration)
      newCofig <- processDialects(config, configuration)
      model    <- parseInput(config, newCofig)
      report   <- report(model, config, newCofig)
    } yield {
      processOutput(report, config)
    }

    res.onComplete {
      case Failure(ex) =>
        config.stderr.print(ex)
        config.proc.exit(ExitCodes.Exception)
      case Success(other) =>
        other
    }

    res
  }

  def report(model: BaseUnit,
             config: ParserConfig,
             configuration: AMFGraphConfiguration): Future[AMFValidationReport] = {
    implicit val executionContext: ExecutionContext = configuration.getExecutionContext
    val customProfileLoaded: Future[ProfileName] = if (config.customProfile.isDefined) {
      // TODO: validation profile is present in config, no clear way to obtain the profile name.
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
      RuntimeValidator(model, profileName, resolved = false, new ValidationConfiguration(configuration))
    }
  }

  def processOutput(report: AMFValidationReport, config: ParserConfig)(implicit ec: ExecutionContext): Unit = {
    val json = ValidationReportJSONLDEmitter.emitJSON(report)
    config.output match {
      case Some(f) => platform.write(f, json)
      case None    => config.stdout.print(json)
    }
    if (!report.conforms) {
      config.proc.exit(ExitCodes.FailingValidation)
    }
  }

}

object ValidateCommand {
  def apply(platform: Platform) = new ValidateCommand(platform)
}
