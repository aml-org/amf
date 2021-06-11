package amf.client.exported
import amf.client.convert.ApiClientConverters._
import amf.client.environment.{
  AMFConfiguration => InternalAMFConfiguration,
  AsyncAPIConfiguration => InternalAsyncAPIConfiguration,
  OASConfiguration => InternalOASConfiguration,
  RAMLConfiguration => InternalRAMLConfiguration,
  WebAPIConfiguration => InternalWebAPIConfiguration
}
import amf.client.model.document.Dialect
import amf.core.client.common.validation.ValidationProfile
import amf.core.client.platform.config.{AMFEventListener, AMFLogger, ParsingOptions, RenderOptions}
import amf.core.client.platform.errorhandling.ErrorHandlerProvider
import amf.core.client.platform.reference.UnitCache
import amf.core.client.platform.resource.ResourceLoader
import amf.core.client.scala.transform.pipelines.TransformationPipeline

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
class AMFConfiguration private[amf] (private[amf] override val _internal: InternalAMFConfiguration)
    extends BaseAMLConfiguration(_internal) {

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

  override def withDialect(dialect: Dialect): AMFConfiguration = _internal.withDialect(asInternal(dialect))

  def withCustomValidationsEnabled(): ClientFuture[AMFConfiguration] =
    _internal.withCustomValidationsEnabled().asClient

  def withDialect(path: String): ClientFuture[AMFConfiguration] = _internal.withDialect(path).asClient

  def withCustomProfile(instancePath: String): ClientFuture[AMFConfiguration] =
    _internal.withCustomProfile(instancePath).asClient

  def withCustomProfile(profile: ValidationProfile): AMFConfiguration = _internal.withCustomProfile(profile)
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
