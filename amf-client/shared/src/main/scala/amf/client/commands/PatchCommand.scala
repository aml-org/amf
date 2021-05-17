package amf.client.commands
import amf.client.remod.AMFGraphConfiguration
import amf.core.client.{ExitCodes, ParserConfig}
import amf.core.model.document.BaseUnit
import amf.core.remote.Platform
import amf.plugins.document.vocabularies.model.document.DialectInstancePatch

import scala.concurrent.Future
import scala.util.{Failure, Success}

class PatchCommand(override val platform: Platform, override val configuration: AMFGraphConfiguration)
    extends TranslateCommand(platform, configuration) {

  override def run(origConfig: ParserConfig): Future[Any] = {
    val config =
      origConfig.copy(outputFormat = Some("AML 1.0"), outputMediaType = Some("application/yaml"), resolve = true)
    val res = for {
      _         <- AMFInit()
      _         <- processDialects(config)
      model     <- parseInput(config)
      _         <- checkValidation(config, model)
      model     <- rewriteTarget(config, model)
      model     <- resolve(config, model)
      generated <- generateOutput(config, model)
    } yield {
      generated
    }

    res.onComplete {

      case Failure(ex: Throwable) =>
        config.stderr.print(ex)
        config.proc.exit(ExitCodes.Exception)
      case Success(other) => other
    }

    res
  }

  protected def rewriteTarget(config: ParserConfig, model: BaseUnit): Future[BaseUnit] = Future {
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
  def apply(platform: Platform, configuration: AMFGraphConfiguration) = new PatchCommand(platform, configuration)
}
