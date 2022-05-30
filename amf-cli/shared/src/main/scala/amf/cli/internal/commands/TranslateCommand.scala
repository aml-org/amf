package amf.cli.internal.commands

import amf.aml.client.scala.AMLConfiguration
import amf.aml.client.scala.model.document.DialectInstance
import amf.aml.internal.parse.plugin.AMLDialectInstanceParsingPlugin
import amf.apicontract.client.scala.AMFConfiguration
import amf.cli.internal.commands.ConfigProvider.configFor
import amf.core.client.common.validation.ProfileName
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.remote.{Platform, Spec}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class TranslateCommand(override val platform: Platform) extends CommandHelper {

  def run(parserConfig: ParserConfig, configuration: AMLConfiguration): Future[Any] = {
    implicit val context: ExecutionContext = configuration.getExecutionContext
    val res: Future[Any] = for {
      _               <- AMFInit(configuration)
      (model, specId) <- parseInput(parserConfig, configuration)
      _               <- checkValidation(parserConfig, model, specId, configuration)
      model           <- resolve(parserConfig, model, specId, configuration)
      generated       <- generateOutput(parserConfig, model, configuration)
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

  def checkValidation(
      parserConfig: ParserConfig,
      model: BaseUnit,
      specId: Spec,
      configuration: AMLConfiguration
  ): Future[Unit] = {
    implicit val context: ExecutionContext = configuration.getExecutionContext

    val dialects = configuration.configurationState().getDialects()
    val validationConfig = dialects.foldLeft(configFor(specId)) { (acc, curr) =>
      acc.withDialect(curr)
    }
    validationConfig.baseUnitClient().validate(model) map { report =>
      if (!report.conforms) {
        parserConfig.stderr.print(report.toString)
        parserConfig.proc.exit(ExitCodes.FailingValidation)
      }
    }
  }
}

object TranslateCommand {
  def apply(platform: Platform) = new TranslateCommand(platform)
}
