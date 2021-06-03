package amf.cli.commands

import amf.ProfileName
import amf.client.environment.AMFConfiguration
import amf.client.remod.parsing.AMLDialectInstanceParsingPlugin
import amf.core.client.{ExitCodes, ParserConfig}
import amf.core.model.document.BaseUnit
import amf.core.remote.Platform
import amf.core.validation.AMFValidationReport
import amf.plugins.document.vocabularies.custom.ParsedValidationProfile
import amf.plugins.document.vocabularies.model.document.{Dialect, DialectInstance}
import amf.plugins.document.vocabularies.model.domain.DialectDomainElement
import amf.plugins.features.validation.emitters.ValidationReportJSONLDEmitter

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class ValidateCommand(override val platform: Platform) extends CommandHelper {

  def run(config: ParserConfig, configuration: AMFConfiguration): Future[Any] = {
    implicit val context: ExecutionContext = configuration.getExecutionContext
    val res = for {
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

  // TODO ARM: move to registry? or to contxt parsng? discuss with tomi
  def findDialect(configuration: AMFConfiguration, id: String): Option[Dialect] = {
    configuration.registry.plugins.parsePlugins.collectFirst({
      case aml: AMLDialectInstanceParsingPlugin if aml.dialect.id == id => aml.dialect
    })
  }

  def report(model: BaseUnit, config: ParserConfig, configuration: AMFConfiguration): Future[AMFValidationReport] = {
    implicit val executionContext: ExecutionContext = configuration.getExecutionContext
    val customProfileLoaded: Future[(ProfileName, AMFConfiguration)] = if (config.customProfile.isDefined) {
      for {
        confCustom    <- configuration.withCustomValidationsEnabled
        customProfile <- confCustom.createClient().parseDialectInstance(config.customProfile.get)
      } yield {
        val profile = ParsedValidationProfile(customProfile.dialectInstance.encodes.asInstanceOf[DialectDomainElement])
        (profile.name, confCustom.withValidationProfile(profile))
      }
    } else {
      Future {
        model match {
          case dialectInstance: DialectInstance =>
            findDialect(configuration, dialectInstance.definedBy().value()) match {
              case Some(dialect) =>
                (ProfileName(dialect.nameAndVersion()), configuration)
              case _ =>
                (config.profile, configuration)
            }
          case _ =>
            (config.profile, configuration)
        }
      }
    }
    customProfileLoaded flatMap {
      case (profileName, conf) =>
        conf.createClient().validate(model, profileName)
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
