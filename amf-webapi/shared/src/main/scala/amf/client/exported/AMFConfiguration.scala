package amf.client.exported
import amf.client.environment.{
  AMFConfiguration => InternalAMFConfiguration,
  RAMLConfiguration => InternalRAMLConfiguration,
  OASConfiguration => InternalOASConfiguration,
  WebAPIConfiguration => InternalWebAPIConfiguration,
  AsyncAPIConfiguration => InternalAsyncAPIConfiguration
}
import amf.client.convert.WebApiClientConverters._
import amf.client.convert.TransformationPipelineConverter._
import amf.client.resolve.ClientErrorHandlerConverter._
import amf.client.exported.config.{AMFEventListener, AMFLogger, ParsingOptions, RenderOptions}
import amf.client.exported.transform.TransformationPipeline
import amf.client.reference.UnitCache
import amf.client.resource.ResourceLoader

import scala.concurrent.ExecutionContext
import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
class AMFConfiguration private[amf] (private[amf] override val _internal: InternalAMFConfiguration)
    extends AMLConfiguration(_internal) {
  private implicit val ec: ExecutionContext = _internal.getExecutionContext

  override def createClient(): AMFClient = new AMFClient(this)

  override def withParsingOptions(parsingOptions: ParsingOptions): AMFConfiguration =
    _internal.withParsingOptions(parsingOptions)

  override def withResourceLoader(rl: ResourceLoader): AMFConfiguration =
    _internal.withResourceLoader(ResourceLoaderMatcher.asInternal(rl))

  override def withResourceLoaders(rl: ClientList[ResourceLoader]): AMFConfiguration =
    _internal.withResourceLoaders(rl.asInternal.toList)

  override def withUnitCache(cache: UnitCache): AMFConfiguration =
    _internal.withUnitCache(ReferenceResolverMatcher.asInternal(cache))

  override def withTransformationPipeline(pipeline: TransformationPipeline): AMFConfiguration =
    _internal.withTransformationPipeline(pipeline)

  override def withRenderOptions(renderOptions: RenderOptions): AMFConfiguration =
    _internal.withRenderOptions(renderOptions)

  override def withErrorHandlerProvider(provider: ErrorHandlerProvider): AMFConfiguration =
    _internal.withErrorHandlerProvider(() => provider.errorHandler())

  override def withEventListener(listener: AMFEventListener): AMFConfiguration = _internal.withEventListener(listener)

  override def withLogger(logger: AMFLogger): AMFConfiguration = _internal.withLogger(logger)

  def merge(other: AMFConfiguration): AMFConfiguration = _internal.merge(other)
}

/**
  * common configuration with all configurations needed for RAML like:
  *   - Validation rules
  *   - Parse and emit plugins
  *   - Transformation Pipelines
  */
@JSExportAll
@JSExportTopLevel("RAMLConfiguration")
object RAMLConfiguration {
  def RAML10(): AMFConfiguration = InternalRAMLConfiguration.RAML10()
  def RAML08(): AMFConfiguration = InternalRAMLConfiguration.RAML08()
  def RAML(): AMFConfiguration   = InternalRAMLConfiguration.RAML()
}

/**
  * common configuration with all configurations needed for OAS like:
  *   - Validation rules
  *   - Parse and emit plugins
  *   - Transformation Pipelines
  */
@JSExportAll
@JSExportTopLevel("OASConfiguration")
object OASConfiguration {
  def OAS20(): AMFConfiguration = InternalOASConfiguration.OAS20()
  def OAS30(): AMFConfiguration = InternalOASConfiguration.OAS30()
  def OAS(): AMFConfiguration   = InternalOASConfiguration.OAS()
}

/** Merged [[OASConfiguration]] and [[RAMLConfiguration]] configurations */
@JSExportAll
@JSExportTopLevel("WebAPIConfiguration")
object WebAPIConfiguration {
  def WebAPI(): AMFConfiguration = InternalWebAPIConfiguration.WebAPI()
}

/**
  * common configuration with all configurations needed for AsyncApi like:
  *   - Validation rules
  *   - Parse and emit plugins
  *   - Transformation Pipelines
  */
@JSExportAll
@JSExportTopLevel("AsyncAPIConfiguration")
object AsyncAPIConfiguration {
  def Async20(): AMFConfiguration = InternalAsyncAPIConfiguration.Async20()
}
