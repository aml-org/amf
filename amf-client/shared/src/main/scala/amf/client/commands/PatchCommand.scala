package amf.client.commands
import amf.client.environment.{AMFConfiguration, AMLConfiguration}
import amf.client.remod.AMFGraphConfiguration
import amf.core.client.{ExitCodes, ParserConfig}
import amf.core.model.document.BaseUnit
import amf.core.remote.Platform
import amf.plugins.document.vocabularies.model.document.DialectInstancePatch

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class PatchCommand(override val platform: Platform) extends TranslateCommand(platform) {

  override def run(parserConfig: ParserConfig, configuration: AMFConfiguration): Future[Any] = {
    implicit val context: ExecutionContext = configuration.getExecutionContext
    val parsingConfig =
      parserConfig.copy(outputFormat = Some("AML 1.0"), outputMediaType = Some("application/yaml"), resolve = true)
    val res = for {
      newConfig <- processDialects(parsingConfig, configuration)
      model     <- parseInput(parsingConfig, newConfig)
      _         <- checkValidation(parsingConfig, model, configuration)
      model     <- rewriteTarget(parsingConfig, model)
      model     <- resolve(parsingConfig, model, configuration)
      generated <- generateOutput(parsingConfig, model, configuration)
    } yield {
      generated
    }

    res.onComplete {

      case Failure(ex: Throwable) =>
        parsingConfig.stderr.print(ex)
        parsingConfig.proc.exit(ExitCodes.Exception)
      case Success(other) => other
    }

    res
  }

  protected def rewriteTarget(config: ParserConfig, model: BaseUnit)(implicit ec: ExecutionContext): Future[BaseUnit] =
    Future {
      model match {
        case patchInstance: DialectInstancePatch =>
          config.patchTarget match {
            case Some(location) =>
              patchInstance.withExtendsModel(platform.resolvePath(location))
            case _ => patchInstance
          }
        case _ =>
          model
      }
    }

}

object PatchCommand {
  def apply(platform: Platform) = new PatchCommand(platform)
}
