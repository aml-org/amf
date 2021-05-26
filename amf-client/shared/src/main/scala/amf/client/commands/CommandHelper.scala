package amf.client.commands

import amf.client.convert.WebApiRegister
import amf.client.environment.AMLConfiguration
import amf.client.remod.amfcore.config.RenderOptions
import amf.client.remod.amfcore.resolution.PipelineName
import amf.client.remod.{AMFGraphConfiguration, ParseConfiguration}
import amf.core.client.ParserConfig
import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote._
import amf.core.resolution.pipelines.TransformationPipeline
import amf.core.services.{RuntimeCompiler, RuntimeResolver, RuntimeSerializer}
import amf.plugins.document.Vocabularies
import amf.plugins.document.vocabularies.AMLPlugin
import amf.plugins.document.webapi.validation.PayloadValidatorPlugin
import amf.plugins.document.webapi._
import amf.plugins.domain.VocabulariesRegister
import amf.plugins.features.validation.custom.AMFValidatorPlugin

import scala.concurrent.{ExecutionContext, Future}

trait CommandHelper {
  val platform: Platform

  def AMFInit(configuration: AMFGraphConfiguration): Future[Unit] = {
    implicit val context: ExecutionContext = configuration.getExecutionContext
    WebApiRegister.register(platform)
    VocabulariesRegister.register(platform) // validation dialect was not being parsed by static config.
    amf.core.AMF.registerPlugin(AMLPlugin)
    amf.core.AMF.registerPlugin(Raml10Plugin)
    amf.core.AMF.registerPlugin(Raml08Plugin)
    amf.core.AMF.registerPlugin(Oas20Plugin)
    amf.core.AMF.registerPlugin(Oas30Plugin)
    amf.core.AMF.registerPlugin(Async20Plugin)
    amf.core.AMF.registerPlugin(AMFValidatorPlugin)
    amf.core.AMF.registerPlugin(PayloadValidatorPlugin)
    amf.core.AMF.init()
  }

  def ensureUrl(inputFile: String): String =
    if (inputFile.startsWith("file:") || inputFile.startsWith("http:") || inputFile.startsWith("https:")) inputFile
    else if (inputFile.startsWith("/")) s"file:/$inputFile"
    else s"file://$inputFile"

  protected def processDialects(config: ParserConfig, configuration: AMLConfiguration): Future[AMLConfiguration] = {
    implicit val context: ExecutionContext = configuration.getExecutionContext
    val dialectFutures                     = config.dialects.map(dialect => AMLPlugin().registry.registerDialect(dialect))
    Future.sequence(dialectFutures) map (dialects =>
      dialects.foldLeft(configuration) {
        case (conf, dialect) => conf.withDialect(dialect)
      })
  }

  protected def parseInput(config: ParserConfig, configuration: AMLConfiguration): Future[BaseUnit] = {
    implicit val context: ExecutionContext = configuration.getExecutionContext
    val inputFile                          = ensureUrl(config.input.get)
    val configClient                       = configuration.createClient()
    val parsed                             = configClient.parse(inputFile)
    val vendor                             = effectiveVendor(config.inputFormat)
    if (config.resolve)
      parsed map (result => {
        val transformed =
          configClient.transform(result.bu, PipelineName.from(vendor, TransformationPipeline.DEFAULT_PIPELINE))
        transformed.bu
      })
    else parsed.map(_.bu)
  }

  protected def resolve(config: ParserConfig, unit: BaseUnit, configuration: AMFGraphConfiguration): Future[BaseUnit] = {
    implicit val context: ExecutionContext = configuration.getExecutionContext
    val configClient                       = configuration.createClient()
    val vendor                             = effectiveVendor(config.inputFormat)
    if (config.resolve && config.validate) {
      val inputFile = ensureUrl(config.input.get)
      val parsed = RuntimeCompiler(
        inputFile,
        Option(effectiveMediaType(config.inputMediaType, config.inputFormat)),
        Context(platform),
        cache = Cache(),
        ParseConfiguration(configuration)
      )
      parsed map { parsed =>
        configClient.transform(parsed, PipelineName.from(vendor, TransformationPipeline.DEFAULT_PIPELINE)).bu
      }
    } else if (config.resolve) {
      Future { configClient.transform(unit, PipelineName.from(vendor, TransformationPipeline.DEFAULT_PIPELINE)).bu }
    } else {
      Future { unit }
    }
  }

  protected def generateOutput(config: ParserConfig,
                               unit: BaseUnit,
                               configuration: AMFGraphConfiguration): Future[Unit] = {
    implicit val context: ExecutionContext = configuration.getExecutionContext
    var generateOptions                    = RenderOptions()
    if (config.withSourceMaps) {
      generateOptions = generateOptions.withSourceMaps
    }
    if (config.withCompactNamespaces) {
      generateOptions = generateOptions.withCompactUris
    }
    val vendor    = effectiveVendor(config.outputFormat)
    val mediaType = effectiveMediaType(config.outputMediaType, config.outputFormat) // TODO: media type not taken into account!
    configuration.withRenderOptions(generateOptions).createClient().render(unit, Vendor(vendor).mediaType).map {
      result =>
        config.output match {
          case Some(f) =>
            platform.write(f, result)
          case None =>
            config.stdout.print(result)
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
