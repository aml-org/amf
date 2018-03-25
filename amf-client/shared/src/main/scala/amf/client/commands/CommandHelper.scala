package amf.client.commands

import amf.client.render.RenderOptions
import amf.core.client.ParserConfig
import amf.core.model.document.BaseUnit
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote._
import amf.core.services.{RuntimeCompiler, RuntimeSerializer}
import amf.plugins.document.vocabularies.RAMLVocabulariesPlugin
import amf.plugins.document.webapi.validation.PayloadValidatorPlugin
import amf.plugins.document.webapi.{OAS20Plugin, OAS30Plugin, RAML08Plugin, RAML10Plugin}
import amf.plugins.features.validation.AMFValidatorPlugin

import scala.concurrent.{ExecutionContext, Future}

trait CommandHelper {
  implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
  val platform: Platform

  def AMFInit(): Future[Unit] = {
    amf.core.AMF.registerPlugin(RAMLVocabulariesPlugin)
    amf.core.AMF.registerPlugin(RAML10Plugin)
    amf.core.AMF.registerPlugin(RAML08Plugin)
    amf.core.AMF.registerPlugin(OAS20Plugin)
    amf.core.AMF.registerPlugin(OAS30Plugin)
    amf.core.AMF.registerPlugin(AMFValidatorPlugin)
    amf.core.AMF.registerPlugin(PayloadValidatorPlugin)
    amf.core.AMF.init()
  }

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

  protected def processDialects(config: ParserConfig): Future[Unit] = {
    val dialectFutures = config.dialects.map { dialect =>
      RAMLVocabulariesPlugin.registry.registerDialect(dialect)
    }
    Future.sequence(dialectFutures).map[Unit] { _ =>
      }
  }

  protected def parseInput(config: ParserConfig): Future[BaseUnit] = {
    var inputFile   = ensureUrl(config.input.get)
    val inputFormat = config.inputFormat.get
    RuntimeCompiler(
      inputFile,
      Option(effectiveMediaType(config.inputMediaType, config.inputFormat)),
      effectiveVendor(config.inputMediaType, config.inputFormat),
      Context(platform)
    )
  }

  protected def generateOutput(config: ParserConfig, unit: BaseUnit): Future[Unit] = {
    val generateOptions = RenderOptions()
    if (config.withSourceMaps) {
      generateOptions.withSourceMaps
    }
    val mediaType = effectiveMediaType(config.outputMediaType, config.outputFormat)
    val vendor    = effectiveVendor(config.outputMediaType, config.outputFormat)
    config.output match {
      case Some(f) =>
        RuntimeSerializer.dumpToFile(
          platform,
          f,
          unit,
          mediaType,
          vendor,
          generateOptions
        )
      case None =>
        Future {
          config.stdout.print(
            RuntimeSerializer(
              unit,
              mediaType,
              vendor,
              generateOptions
            )
          )
        }
    }
  }

  def effectiveMediaType(mediaType: Option[String], vendor: Option[String]) = {
    mediaType match {
      case Some(effectiveMediaType) => effectiveMediaType
      case None =>
        vendor match {
          case Some(effectiveVendor) if AMFPluginsRegistry.documentPluginForID(effectiveVendor).isDefined =>
            AMFPluginsRegistry.documentPluginForID(effectiveVendor).get.documentSyntaxes.head
          case _ => "*/*"
        }
    }
  }

  def effectiveVendor(mediaType: Option[String], vendor: Option[String]): String = {
    vendor match {
      case Some(effectiveVendor) => effectiveVendor
      case None                  => "Unknown"
    }
  }

}
