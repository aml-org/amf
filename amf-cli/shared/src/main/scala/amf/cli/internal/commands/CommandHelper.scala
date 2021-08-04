package amf.cli.internal.commands

import amf.aml.client.scala.AMLConfiguration
import amf.aml.internal.utils.VocabulariesRegister
import amf.apicontract.client.scala.{AMFConfiguration, AsyncAPIConfiguration, OASConfiguration, RAMLConfiguration}
import amf.apicontract.internal.convert.ApiRegister
import amf.core.client.common.transform._
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.parser.{AMFCompiler, CompilerConfiguration}
import amf.core.internal.remote.{Cache, Context, Platform, Spec}

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
    val dialectFutures                     = config.dialects.map(dialect => configuration.baseUnitClient().parseDialect(dialect))
    Future.sequence(dialectFutures) map (results =>
      results.foldLeft(configuration) {
        case (conf, result) => conf.withDialect(result.dialect)
      })
  }

  protected def parseInput(config: ParserConfig, configuration: AMLConfiguration): Future[(BaseUnit, Spec)] = {
    implicit val context: ExecutionContext = configuration.getExecutionContext
    val inputFile                          = ensureUrl(config.input.get)
    val configClient                       = configuration.baseUnitClient()
    val parseResult                        = configClient.parse(inputFile)
    if (config.resolve)
      parseResult map (result => {
        val transformed = configClient.transform(result.baseUnit, PipelineId.Default)
        (transformed.baseUnit, result.sourceSpec)
      })
    else parseResult.map(result => (result.baseUnit, result.sourceSpec))
  }

  protected def resolve(config: ParserConfig,
                        unit: BaseUnit,
                        specId: Spec,
                        configuration: AMFGraphConfiguration): Future[BaseUnit] = {
    implicit val context: ExecutionContext = configuration.getExecutionContext
    val configClient                       = configuration.baseUnitClient()
    if (config.resolve && config.validate) {
      val inputFile = ensureUrl(config.input.get)
      val parsed = AMFCompiler(
        inputFile,
        Context(platform),
        cache = Cache(),
        CompilerConfiguration(configuration)
      ).build()
      parsed map { parsed =>
        configClient.transform(parsed, PipelineId.Default).baseUnit
      }
    } else if (config.resolve) {
      Future {
        configClient.transform(unit, PipelineId.Default).baseUnit
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
    val spec         = effectiveVendor(config.outputFormat)
    val renderConfig = configFor(spec)
    val result =
      renderConfig
        .getOrElse(configuration)
        .withRenderOptions(generateOptions)
        .baseUnitClient()
        .render(unit)
    config.output match {
      case Some(f) =>
        platform.write(f, result)
      case None =>
        Future.successful(config.stdout.print(result))
    }
  }

  def effectiveMediaType(mediaType: Option[String], spec: Option[String]): String = {
    mediaType match {
      case Some(effectiveMediaType) => effectiveMediaType
      case None =>
        spec match {
//          case Some(effectiveVendor) if AMFPluginsRegistry.documentPluginForID(effectiveVendor).isDefined =>
//            AMFPluginsRegistry.documentPluginForID(effectiveVendor).get.documentSyntaxes.head
          case _ => "*/*"
        }
    }
  }

  private def configFor(spec: Spec): Option[AMFConfiguration] = spec match {
    case Spec.RAML10  => Some(RAMLConfiguration.RAML10())
    case Spec.RAML08  => Some(RAMLConfiguration.RAML08())
    case Spec.OAS20   => Some(OASConfiguration.OAS20())
    case Spec.OAS30   => Some(OASConfiguration.OAS30())
    case Spec.ASYNC20 => Some(AsyncAPIConfiguration.Async20())
    case _            => None
  }

  def effectiveVendor(spec: Option[String]): Spec = spec.flatMap(Spec.unapply).getOrElse(Spec("unknown"))

}
