package amf.client.commands

import amf.core.client.{ExitCodes, ParserConfig}
import amf.core.model.document.BaseUnit
import amf.core.remote.Platform
import amf.core.services.RuntimeValidator
import amf.core.validation.AMFValidationReport
import amf.plugins.features.validation.emitters.ValidationReportJSONLDEmitter

import scala.concurrent.Future
import scala.util.{Failure, Success}

class ValidateCommand(override val platform: Platform) extends CommandHelper {

  def run(config: ParserConfig): Future[Any] = {
    val res = for {
      _          <- AMFInit()
      _          <- processDialects(config)
      model      <- parseInput(config)
      report     <- report(model, config)
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


  def report(model: BaseUnit, config: ParserConfig) = {
    val customProfileLoaded = if (config.customProfile.isDefined) {
      RuntimeValidator.loadValidationProfile(config.customProfile.get) map { profileName =>
        profileName
      }
    } else {
      Future {
        config.validationProfile
      }
    }
    customProfileLoaded flatMap  { profileName =>
      RuntimeValidator(model, profileName)
    }
  }

  def processOutput(report: AMFValidationReport, config: ParserConfig) = {
    val json = ValidationReportJSONLDEmitter.emitJSON(report)
    config.output match {
      case Some(f) => {
        platform.write(f, json)
      }
      case None => {
        config.stdout.print(json)
      }
    }
    if (!report.conforms) {
      config.proc.exit(ExitCodes.FailingValidation)
    }
  }

}

object ValidateCommand {
  def apply(platform: Platform) = new ValidateCommand(platform)
}
