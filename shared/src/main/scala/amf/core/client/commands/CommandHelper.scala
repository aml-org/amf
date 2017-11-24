package amf.core.client.commands

import amf.ProfileNames
import amf.compiler.AMFCompiler
import amf.core.client.{GenerationOptions, ParserConfig}
import amf.dumper.AMFDumper
import amf.framework.model.document.BaseUnit
import amf.framework.remote.Syntax.{Json, Yaml}
import amf.framework.remote._
import amf.validation.Validation

import scala.concurrent.{ExecutionContext, Future, Promise}

trait CommandHelper {
  implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
  val platform: Platform

  def ensureUrl(inputFile: String): String =
    if (!inputFile.startsWith("file:") && !inputFile.startsWith("http:") && !inputFile.startsWith("https:")) {
      if (inputFile.startsWith("/")) {
        s"file:/$inputFile"
      } else {
        s"file://$inputFile"
      }
    } else {
      inputFile
    }

  def setupValidation(config: ParserConfig): Future[Validation] = {
    val currentValidation = new Validation(platform)
    currentValidation.loadValidationDialect() flatMap { loadedDialect =>
      config.customProfile match {
        case Some(profileFile) => currentValidation.loadValidationProfile(profileFile).map(_ => currentValidation)
        case _                 => Promise().success(currentValidation).future
      }
    }
  }

  protected def processDialects(config: ParserConfig): Future[Unit] = {
    val dialectFutures = config.dialects.map { dialect =>
      platform.dialectsRegistry.registerDialect(ensureUrl(dialect))
    }
    Future.sequence(dialectFutures).map[Unit] { _ =>
      }
  }

  protected def parseInput(config: ParserConfig, currentValidation: Validation): Future[BaseUnit] = {
    var inputFile   = ensureUrl(config.input.get)
    val inputFormat = config.inputFormat.get

    val hint = inputFormat match {
      case ProfileNames.RAML => RamlYamlHint
      case ProfileNames.OAS  => OasJsonHint
      case ProfileNames.AMF  => AmfJsonHint
    }
    AMFCompiler(inputFile, platform, hint, currentValidation, None, None).build()
  }

  protected def generateOutput(config: ParserConfig, unit: BaseUnit): Future[Unit] = {
    val outputFormat = config.outputFormat.get match {
      case ProfileNames.RAML => Raml
      case ProfileNames.OAS  => Oas
      case ProfileNames.AMF  => Amf
    }

    val hint = config.outputFormat.get match {
      case ProfileNames.RAML => Yaml
      case ProfileNames.OAS  => Json
      case ProfileNames.AMF  => Json
    }

    val generateOptions = GenerationOptions()
    if (config.withSourceMaps) {
      generateOptions.withSourceMaps
    }

    val dumper = AMFDumper(unit, outputFormat, hint, generateOptions)
    config.output match {
      case Some(f) => dumper.dumpToFile(platform, f)
      case None    => Future { println(dumper.dumpToString) }
    }
  }

}
