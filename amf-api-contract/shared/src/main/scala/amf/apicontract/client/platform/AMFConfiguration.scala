package amf.apicontract.client.platform

import amf.aml.client.platform.model.document.Dialect
import amf.aml.client.platform.model.document.DialectInstance
import amf.aml.internal.convert.VocabulariesClientConverter.DialectConverter
import amf.apicontract.client.scala.{
  APIConfiguration => InternalAPIConfiguration,
  AsyncAPIConfiguration => InternalAsyncAPIConfiguration,
  OASConfiguration => InternalOASConfiguration,
  RAMLConfiguration => InternalRAMLConfiguration,
  WebAPIConfiguration => InternalWebAPIConfiguration
}
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.core.client.platform.config.{AMFEventListener, ParsingOptions, RenderOptions}
import amf.core.client.platform.errorhandling.ErrorHandlerProvider
import amf.core.client.platform.reference.UnitCache
import amf.core.client.platform.resource.ResourceLoader
import amf.core.client.platform.transform.TransformationPipeline
import amf.core.internal.convert.ClientErrorHandlerConverter._
import amf.core.internal.convert.TransformationPipelineConverter._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import amf.apicontract.client.scala
import amf.core.client.platform.execution.BaseExecutionEnvironment
import amf.core.client.platform.validation.payload.AMFShapePayloadValidationPlugin
import amf.core.internal.convert.PayloadValidationPluginConverter.PayloadValidationPluginMatcher
import amf.core.internal.remote.Spec
import amf.shapes.client.platform.BaseShapesConfiguration

/**
  * The AMFConfiguration lets you customize all AMF-specific configurations.
  * Its immutable and created through builders. An instance is needed to use AMF.
  * @see [[AMFBaseUnitClient]]
  */
@JSExportAll
class AMFConfiguration private[amf] (private[amf] override val _internal: scala.AMFConfiguration)
    extends BaseShapesConfiguration(_internal) {

  /** Contains common AMF graph operations associated to documents */
  override def baseUnitClient(): AMFBaseUnitClient = new AMFBaseUnitClient(this)

  /** Contains functionality associated with specific elements of the AMF model */
  override def elementClient(): AMFElementClient = new AMFElementClient(this)

  /** Contains methods to get information about the current state of the configuration */
  def configurationState(): AMFConfigurationState = new AMFConfigurationState(this)

  /**
    * Set [[ParsingOptions]]
    * @param parsingOptions [[ParsingOptions]] to add to configuration object
    * @return [[AMFConfiguration]] with [[ParsingOptions]] added
    */
  override def withParsingOptions(parsingOptions: ParsingOptions): AMFConfiguration =
    _internal.withParsingOptions(parsingOptions)

  /**
    * Add a [[ResourceLoader]]
    * @param rl [[ResourceLoader]] to add to configuration object
    * @return [[AMFConfiguration]] with the [[ResourceLoader]] added
    */
  override def withResourceLoader(rl: ResourceLoader): AMFConfiguration =
    _internal.withResourceLoader(ResourceLoaderMatcher.asInternal(rl))

  /**
    * Set the configuration [[ResourceLoader]]s
    * @param rl a list of [[ResourceLoader]] to set to the configuration object
    * @return [[AMFConfiguration]] with [[ResourceLoader]]s set
    */
  override def withResourceLoaders(rl: ClientList[ResourceLoader]): AMFConfiguration =
    _internal.withResourceLoaders(rl.asInternal.toList)

  /**
    * Set [[UnitCache]]
    * @param cache [[UnitCache]] to add to configuration object
    * @return [[AMFConfiguration]] with [[UnitCache]] added
    */
  override def withUnitCache(cache: UnitCache): AMFConfiguration =
    _internal.withUnitCache(UnitCacheMatcher.asInternal(cache))

  /**
    * Add a [[TransformationPipeline]]
    * @param pipeline [[TransformationPipeline]] to add to configuration object
    * @return [[AMFConfiguration]] with [[TransformationPipeline]] added
    */
  override def withTransformationPipeline(pipeline: TransformationPipeline): AMFConfiguration =
    _internal.withTransformationPipeline(pipeline)

  /**
    * Set [[RenderOptions]]
    * @param renderOptions [[RenderOptions]] to set to configuration object
    * @return [[AMFConfiguration]] with [[RenderOptions]] added
    */
  override def withRenderOptions(renderOptions: RenderOptions): AMFConfiguration =
    _internal.withRenderOptions(renderOptions)

  /**
    * Set [[ErrorHandlerProvider]]
    * @param provider [[ErrorHandlerProvider]] to set to configuration object
    * @return [[AMFConfiguration]] with [[ErrorHandlerProvider]] set
    */
  override def withErrorHandlerProvider(provider: ErrorHandlerProvider): AMFConfiguration =
    _internal.withErrorHandlerProvider(() => provider.errorHandler())

  /**
    * Add an [[AMFEventListener]]
    * @param listener [[AMFEventListener]] to add to configuration object
    * @return [[AMFConfiguration]] with [[AMFEventListener]] added
    */
  override def withEventListener(listener: AMFEventListener): AMFConfiguration = _internal.withEventListener(listener)

  /**
    * Set [[BaseExecutionEnvironment]]
    * @param executionEnv [[BaseExecutionEnvironment]] to set to configuration object
    * @return [[AMFConfiguration]] with [[BaseExecutionEnvironment]] set
    */
  override def withExecutionEnvironment(executionEnv: BaseExecutionEnvironment): AMFConfiguration =
    _internal.withExecutionEnvironment(executionEnv._internal)

  /**
    * Register a Dialect
    * @param dialect [[Dialect]] to register
    * @return [[AMFConfiguration]] with [[Dialect]] registered
    */
  override def withDialect(dialect: Dialect): AMFConfiguration = _internal.withDialect(asInternal(dialect))

  /**
    * Register a Dialect
    * @param url URL of the Dialect to register
    * @return A CompletableFuture of [[AMFConfiguration]]
    */
  def withDialect(url: String): ClientFuture[AMFConfiguration] = _internal.withDialect(url).asClient

  /**
    * Register a [[Dialect]] linked from a [[DialectInstance]]
    * @param url of the [[DialectInstance]]
    * @return A CompletableFuture of [[AMFConfiguration]]
    */
  def forInstance(url: String): ClientFuture[AMFConfiguration] = _internal.forInstance(url).asClient

  override def withShapePayloadPlugin(plugin: AMFShapePayloadValidationPlugin): AMFConfiguration =
    _internal.withPlugin(PayloadValidationPluginMatcher.asInternal(plugin))
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
  def RAML10(): AMFConfiguration             = InternalRAMLConfiguration.RAML10()
  def RAML08(): AMFConfiguration             = InternalRAMLConfiguration.RAML08()
  def RAML(): AMFConfiguration               = InternalRAMLConfiguration.RAML()
  def fromSpec(spec: Spec): AMFConfiguration = InternalRAMLConfiguration.fromSpec(spec)
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
  def OAS20(): AMFConfiguration              = InternalOASConfiguration.OAS20()
  def OAS30(): AMFConfiguration              = InternalOASConfiguration.OAS30()
  def OAS(): AMFConfiguration                = InternalOASConfiguration.OAS()
  def fromSpec(spec: Spec): AMFConfiguration = InternalOASConfiguration.fromSpec(spec)
}

/** Merged [[OASConfiguration]] and [[RAMLConfiguration]] configurations */
@JSExportAll
@JSExportTopLevel("WebAPIConfiguration")
object WebAPIConfiguration {
  def WebAPI(): AMFConfiguration             = InternalWebAPIConfiguration.WebAPI()
  def fromSpec(spec: Spec): AMFConfiguration = InternalWebAPIConfiguration.fromSpec(spec)
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

@JSExportAll
@JSExportTopLevel("APIConfiguration")
object APIConfiguration {
  def API(): AMFConfiguration                = InternalAPIConfiguration.API()
  def fromSpec(spec: Spec): AMFConfiguration = InternalAPIConfiguration.fromSpec(spec)
}
