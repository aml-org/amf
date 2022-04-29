package amf.cli.internal.commands

import amf.aml.client.scala.AMLConfiguration
import amf.aml.client.scala.model.document.{Dialect, DialectInstance}
import amf.aml.client.scala.model.domain.DialectDomainElement
import amf.aml.internal.parse.plugin.AMLDialectInstanceParsingPlugin
import amf.apicontract.client.scala.AMFConfiguration
import amf.core.client.common.validation.ProfileName
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.validation.AMFValidationReport
import amf.core.internal.remote.Platform
import amf.validation.internal.emitters.ValidationReportJSONLDEmitter
import com.github.ghik.silencer.silent

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class ValidateCommand(override val platform: Platform) extends CommandHelper {

  def run(parserConfig: ParserConfig, configuration: AMLConfiguration): Future[Any] = {
    implicit val context: ExecutionContext = configuration.getExecutionContext
    val res = for {
      newConfig  <- processDialects(parserConfig, configuration)
      (model, _) <- parseInput(parserConfig, newConfig)
      report     <- report(model, parserConfig, newConfig)
    } yield {
      processOutput(report, parserConfig)
    }

    res.onComplete {
      case Failure(ex) =>
        parserConfig.stderr.print(ex)
        parserConfig.proc.exit(ExitCodes.Exception)
      case Success(other) =>
        other
    }

    res
  }

  def findDialect(configuration: AMLConfiguration, id: String): Option[Dialect] = {
    configuration.configurationState().getDialects().find(_.id == id)
  }

  def report(model: BaseUnit, config: ParserConfig, configuration: AMLConfiguration): Future[AMFValidationReport] = {
    implicit val executionContext: ExecutionContext = configuration.getExecutionContext
    val customProfileLoaded: Future[(ProfileName, AMLConfiguration)] = Future {
      model match {
        case dialectInstance: DialectInstance =>
          @silent("deprecated") // Silent can only be used in assignment expressions
          val definedBy =
            dialectInstance.processingData
              .definedBy()
              .option()
              .orElse(dialectInstance.processingData.definedBy().option())
              .orNull
          findDialect(configuration, definedBy) match {
            case Some(dialect) =>
              (ProfileName(dialect.nameAndVersion()), configuration)
            case _ =>
              (config.profile, configuration)
          }
        case _ =>
          (config.profile, configuration)
      }
    }
    customProfileLoaded flatMap { case (profileName, conf) =>
      conf.baseUnitClient().validate(model)
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
