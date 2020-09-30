package amf.client.commands

import amf.client.convert.WebApiRegister
import amf.core.client.ParserConfig
import amf.core.emitter.RenderOptions
import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.parser.errorhandler.UnhandledParserErrorHandler
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote._
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.services.{RuntimeCompiler, RuntimeResolver, RuntimeSerializer}
import amf.plugins.document.vocabularies.AMLPlugin
import amf.plugins.document.webapi.validation.PayloadValidatorPlugin
import amf.plugins.document.webapi.{Oas20Plugin, Oas30Plugin, Raml08Plugin, Raml10Plugin}
import amf.plugins.features.validation.custom.AMFValidatorPlugin

import scala.concurrent.{ExecutionContext, Future}

trait CommandHelper {
  implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
  val platform: Platform

  def AMFInit(): Future[Unit] = {
    WebApiRegister.register(platform)
    amf.core.AMF.registerPlugin(AMLPlugin)
    amf.core.AMF.registerPlugin(Raml10Plugin)
    amf.core.AMF.registerPlugin(Raml08Plugin)
    amf.core.AMF.registerPlugin(Oas20Plugin)
    amf.core.AMF.registerPlugin(Oas30Plugin)
    amf.core.AMF.registerPlugin(AMFValidatorPlugin)
    amf.core.AMF.registerPlugin(PayloadValidatorPlugin)
    amf.core.AMF.init()
  }

  def ensureUrl(inputFile: String): String =
    if (inputFile.startsWith("file:") || inputFile.startsWith("http:") || inputFile.startsWith("https:")) inputFile
    else if (inputFile.startsWith("/")) s"file:/$inputFile"
    else s"file://$inputFile"

  protected def processDialects(config: ParserConfig): Future[Unit] = {
    val dialectFutures = config.dialects.map(dialect => AMLPlugin().registry.registerDialect(dialect))
    Future.sequence(dialectFutures) map [Unit](_ => {})
  }

  protected def parseInput(config: ParserConfig): Future[BaseUnit] = {
    var inputFile = ensureUrl(config.input.get)
    val parsed = RuntimeCompiler(
      inputFile,
      Option(effectiveMediaType(config.inputMediaType, config.inputFormat)),
      config.inputFormat,
      Context(platform),
      cache = Cache(),
      errorHandler = UnhandledParserErrorHandler
    )
    val vendor = effectiveVendor(config.inputFormat)
    if (config.resolve)
      parsed map (unit =>
        RuntimeResolver.resolve(vendor, unit, ResolutionPipeline.DEFAULT_PIPELINE, UnhandledErrorHandler))
    else parsed
  }

  protected def resolve(config: ParserConfig, unit: BaseUnit): Future[BaseUnit] = {
    val vendor = effectiveVendor(config.inputFormat)
    if (config.resolve && config.validate) {
      var inputFile = ensureUrl(config.input.get)
      val parsed = RuntimeCompiler(
        inputFile,
        Option(effectiveMediaType(config.inputMediaType, config.inputFormat)),
        config.inputFormat,
        Context(platform),
        cache = Cache(),
        errorHandler = UnhandledParserErrorHandler
      )
      parsed map { parsed =>
        RuntimeResolver.resolve(vendor, parsed, ResolutionPipeline.DEFAULT_PIPELINE, UnhandledErrorHandler)
      }
    } else if (config.resolve) {
      Future { RuntimeResolver.resolve(vendor, unit, ResolutionPipeline.DEFAULT_PIPELINE, UnhandledErrorHandler) }
    } else {
      Future { unit }
    }
  }

  protected def generateOutput(config: ParserConfig, unit: BaseUnit): Future[Unit] = {
    val generateOptions = RenderOptions()
    if (config.withSourceMaps) {
      generateOptions.withSourceMaps
    }
    if (config.withCompactNamespaces) {
      generateOptions.withCompactUris
    }
    val mediaType = effectiveMediaType(config.outputMediaType, config.outputFormat)
    val vendor    = effectiveVendor(config.outputFormat)
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

  def effectiveMediaType(mediaType: Option[String], vendor: Option[String]): String = {
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

  def effectiveVendor(vendor: Option[String]): String = {
    vendor match {
      case Some(effectiveVendor) => effectiveVendor
      case None                  => "Unknown"
    }
  }

}
