package amf.core.client.commands

import amf.core.client.ParserConfig
import amf.core.remote.Platform

import scala.concurrent.Future

class ValidateCommand(override val platform: Platform) extends CommandHelper {

  def run(config: ParserConfig): Future[Any] = {
    /*
    val res = for {
      _          <- processDialects(config)
      validation <- setupValidation(config)
      model      <- parseInput(config)
      report     <- report(model, validation, config)
    } yield {
      processOutput(report, config)
    }

    res.onComplete {
      case Failure(ex) =>
        System.err.println(ex)
        platform.exit(ExitCodes.Exception)
      case Success(other) =>
        other
    }
    res
    */
    Future {
      println("Not supported yet")
    }
  }

  /*
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
      case None => {
        println(json)
      }
    }
    if (!report.conforms) {
      platform.exit(ExitCodes.FailingValidation)
    }
  }
  */
}

object ValidateCommand {
  def apply(platform: Platform) = new ValidateCommand(platform)
}
