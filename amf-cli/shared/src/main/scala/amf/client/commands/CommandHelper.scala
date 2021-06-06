package amf.client.commands

import amf.client.convert.ApiRegister
import amf.client.environment.{AMFConfiguration, AMLConfiguration}
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.transform.PipelineName
import amf.core.client.scala.transform.pipelines.TransformationPipeline
import amf.core.internal.parser.{AMFCompiler, ParseConfiguration}
import amf.core.internal.remote.{Cache, Context, Platform, Vendor}
import amf.plugins.domain.VocabulariesRegister

import scala.concurrent.{ExecutionContext, Future}

trait CommandHelper {
  val platform: Platform

  def AMFInit(configuration: AMFGraphConfiguration): Future[Unit] = {
    implicit val context: ExecutionContext = configuration.getExecutionContext
    ApiRegister.register(platform)
    VocabulariesRegister.register(platform) // validation dialect was not being parsed by static config.
    Future.successful {}
  }

  def ensureUrl(inputFile: String): String =
    if (inputFile.startsWith("file:") || inputFile.startsWith("http:") || inputFile.startsWith("https:")) inputFile
    else if (inputFile.startsWith("/")) s"file:/$inputFile"
    else s"file://$inputFile"

  protected def processDialects(config: ParserConfig, configuration: AMFConfiguration): Future[AMFConfiguration] = {
    implicit val context: ExecutionContext = configuration.getExecutionContext
    val dialectFutures                     = config.dialects.map(dialect => configuration.createClient().parseDialect(dialect))
    Future.sequence(dialectFutures) map (results =>
      results.foldLeft(configuration) {
        case (conf, result) => conf.withDialect(result.dialect)
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
          configClient.transform(result.bu,
                                 PipelineName.from(Vendor(vendor).mediaType, TransformationPipeline.DEFAULT_PIPELINE))
        transformed.bu
      })
    else parsed.map(_.bu)
  }

  protected def resolve(config: ParserConfig, unit: BaseUnit, configuration: AMFGraphConfiguration): Future[BaseUnit] = {
    implicit val context: ExecutionContext = configuration.getExecutionContext
    val configClient                       = configuration.createClient()
    val vendor                             = effectiveVendor(config.inputFormat)
    val vendorMediaType                    = Vendor(vendor).mediaType
    if (config.resolve && config.validate) {
      val inputFile = ensureUrl(config.input.get)
      val parsed = AMFCompiler(
        inputFile,
        Option(effectiveMediaType(config.inputMediaType, config.inputFormat)),
        Context(platform),
        cache = Cache(),
        ParseConfiguration(configuration)
      ).build()
      parsed map { parsed =>
        configClient.transform(parsed, PipelineName.from(vendorMediaType, TransformationPipeline.DEFAULT_PIPELINE)).bu
      }
    } else if (config.resolve) {
      Future {
        configClient.transform(unit, PipelineName.from(vendorMediaType, TransformationPipeline.DEFAULT_PIPELINE)).bu
      }
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
    val result    = configuration.withRenderOptions(generateOptions).createClient().render(unit, Vendor(vendor).mediaType)
    config.output match {
      case Some(f) =>
        platform.write(f, result)
      case None =>
        Future.successful(config.stdout.print(result))
    }
  }

  def effectiveMediaType(mediaType: Option[String], vendor: Option[String]): String = {
    mediaType match {
      case Some(effectiveMediaType) => effectiveMediaType
      case None =>
        vendor match {
//          case Some(effectiveVendor) if AMFPluginsRegistry.documentPluginForID(effectiveVendor).isDefined =>
//            AMFPluginsRegistry.documentPluginForID(effectiveVendor).get.documentSyntaxes.head
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
