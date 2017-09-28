package amf.client.commands

import amf.client.{ExitCodes, ParserConfig}
import amf.document.BaseUnit
import amf.remote.Platform
import amf.validation.emitters.ValidationReportJSONLDEmitter
import amf.validation.{AMFValidationReport, Validation}

import scala.concurrent.Future
import scala.util.{Failure, Success}

class ValidateCommand(override val platform: Platform) extends CommandHelper {

  def run(config: ParserConfig): Future[Any] = {
    val res = for {
      validation <- setupValidation(config)
      model <- parseInput(config)
      report <- report(model, validation, config)
    } yield {
      processOutput(report, config)
    }

    res.onComplete {
      case Failure(ex) => {
        System.err.println(ex)
        System.exit(ExitCodes.Exception)
      }
      case Success(other) => {
        other
      }
    }
    res
  }


  def report(model: BaseUnit, validation: Validation, config: ParserConfig) = {
    val profile = validation.profile match {
      case Some(prof) => prof.name
      case None       => config.validationProfile
    }

    validation.validate(model, profile)
  }

  def processOutput(report: AMFValidationReport, config: ParserConfig) = {
    val json = ValidationReportJSONLDEmitter.emitJSON(report)
    config.output match {
      case Some(f) => {
        platform.write(f, json)
      }
      case None    => {
        println(json)
      }
    }
    if (!report.conforms) {
      System.exit(ExitCodes.FailingValidation)
    }
  }
}

object ValidateCommand {
  def apply(platform: Platform) = new ValidateCommand(platform)
}