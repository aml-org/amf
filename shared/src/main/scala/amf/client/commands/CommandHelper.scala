package amf.client.commands

import amf.client.{GenerationOptions, ParserConfig}
import amf.compiler.AMFCompiler
import amf.document.BaseUnit
import amf.dumper.AMFDumper
import amf.remote.Syntax.{Json, Yaml}
import amf.remote._
import amf.validation.{Validation, ValidationProfileNames}

import scala.concurrent.{ExecutionContext, Future, Promise}

trait CommandHelper {
  implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
  val platform: Platform

  def ensureUrl(inputFile: String): String =
    if (!inputFile.startsWith("file:") && !inputFile.startsWith("http:") && !inputFile.startsWith("https:")) {
      if(inputFile.startsWith("/")) {
        s"file:/$inputFile"
      } else {
        s"file://$inputFile"
      }
    } else {
      inputFile
    }

  def setupValidation(config: ParserConfig): Future[Validation] = {
    val validation = Validation(platform)
    validation.loadValidationDialect() flatMap  { loadedDialect =>
      config.customProfile match {
        case Some(profileFile) => validation.loadValidationProfile(profileFile).map(_ => validation)
        case _                 => Promise().success(validation).future
      }
    }
  }

  protected def parseInput(config: ParserConfig): Future[BaseUnit] = {
    var inputFile = ensureUrl(config.input.get)
    val dialectFutures = config.dialects.map { dialect =>
      platform.dialectsRegistry.registerDialect(ensureUrl(dialect))
    }

    Future.sequence(dialectFutures).flatMap[BaseUnit] { _ =>


      val inputFormat = config.inputFormat.get


      val hint = inputFormat match {
        case ValidationProfileNames.RAML => RamlYamlHint
        case ValidationProfileNames.OAS  => OasJsonHint
        case ValidationProfileNames.AMF  => AmfJsonHint
      }
      AMFCompiler(inputFile, platform, hint, None, None, platform.dialectsRegistry).build()
    }
  }

  protected def generateOutput(config: ParserConfig, unit: BaseUnit): Future[String] = {
    val outputFormat = config.outputFormat.get match {
      case ValidationProfileNames.RAML => Raml
      case ValidationProfileNames.OAS  => Oas
      case ValidationProfileNames.AMF  => Amf
    }

    val hint = config.outputFormat.get match {
      case ValidationProfileNames.RAML => Yaml
      case ValidationProfileNames.OAS  => Json
      case ValidationProfileNames.AMF  => Json
    }

    val generateOptions = GenerationOptions()
    if (config.withSourceMaps) {
      generateOptions.withSourceMaps
    }

    val dumper = AMFDumper(unit, outputFormat, hint, generateOptions)
    config.output match {
      case Some(f) => dumper.dumpToFile(platform, f)
      case None    => dumper.dumpToString.map { generated =>
        println(generated)
        generated
      }
    }
  }

}
