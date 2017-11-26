package amf.core.client.commands

import amf.ProfileNames
import amf.core.client.{GenerationOptions, ParserConfig}
import amf.core.model.document.BaseUnit
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote._
import amf.core.services.{RuntimeCompiler, RuntimeSerializer}

import scala.concurrent.{ExecutionContext, Future}

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

  /*
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
  */


  protected def parseInput(config: ParserConfig): Future[BaseUnit] = {
    var inputFile   = ensureUrl(config.input.get)
    val inputFormat = config.inputFormat.get

    val hint = inputFormat match {
      case ProfileNames.RAML => RamlYamlHint
      case ProfileNames.OAS  => OasJsonHint
      case ProfileNames.AMF  => AmfJsonHint
    }
    RuntimeCompiler(
      inputFile,
      platform,
      effectiveMediaType(config.inputMediaType, config.inputFormat),
      effectiveVendor(config.inputMediaType, config.inputFormat)
    )
  }

  protected def generateOutput(config: ParserConfig, unit: BaseUnit): Future[Unit] = {
    val generateOptions = GenerationOptions()
    if (config.withSourceMaps) {
      generateOptions.withSourceMaps
    }
    config.output match {
      case Some(f) =>
        RuntimeSerializer.dumpToFile(
          platform,
          f,
          unit,
          effectiveMediaType(config.inputMediaType, config.inputFormat),
          effectiveVendor(config.inputMediaType, config.inputFormat),
          generateOptions
        )
      case None    => Future {
        println(
          RuntimeSerializer(
            unit,
            effectiveMediaType(config.inputMediaType, config.inputFormat),
            effectiveVendor(config.inputMediaType, config.inputFormat),
            generateOptions
          )
        )
      }
    }
  }

  def effectiveMediaType(mediaType: Option[String], vendor: Option[String]) = {
    mediaType match {
      case Some(effectiveMediaType) => effectiveMediaType
      case None => vendor match {
        case Some(effectiveVendor) if AMFPluginsRegistry.documentPluginForID(effectiveVendor).isDefined =>
          AMFPluginsRegistry.documentPluginForID(effectiveVendor).get.documentSyntaxes.head
        case _ => "*/*"
      }
    }
  }


  def effectiveVendor(mediaType: Option[String], vendor: Option[String]): String = {
    vendor match {
      case Some(effectiveVendor) => effectiveVendor
      case None => "Unknown"
    }
  }

}
