package amf.shapes.client.platform.config

import amf.aml.client.platform.AMLConfigurationState
import amf.aml.client.platform.model.document.{Dialect, DialectInstance}
import amf.aml.internal.convert.VocabulariesClientConverter.{ClientFuture, ClientList}
import amf.core.client.platform.adoption.IdAdopterProvider
import amf.core.client.platform.config.{AMFEventListener, ParsingOptions, RenderOptions}
import amf.core.client.platform.errorhandling.ErrorHandlerProvider
import amf.core.client.platform.execution.BaseExecutionEnvironment
import amf.core.client.platform.reference.UnitCache
import amf.core.client.platform.resource.ResourceLoader
import amf.core.client.platform.transform.TransformationPipeline
import amf.core.client.platform.validation.payload.AMFShapePayloadValidationPlugin
import amf.core.internal.convert.ClientErrorHandlerConverter._
import amf.core.internal.convert.PayloadValidationPluginConverter.PayloadValidationPluginMatcher
import amf.core.internal.convert.TransformationPipelineConverter._
import amf.shapes.client.platform.ShapesElementClient
import amf.shapes.client.scala.{
  JsonSchemaBasedSpecConfiguration => InternalJsonSchemaBasedSpecConfiguration,
  ShapesConfiguration => InternalShapesConfiguration
}
import amf.shapes.internal.convert.ShapeClientConverters._

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class JsonSchemaBasedSpecConfiguration private[amf] (private[amf] override val _internal: InternalJsonSchemaBasedSpecConfiguration)
    extends BaseJsonSchemaBasedSpecConfiguration(_internal) {

  /** Contains functionality associated with specific elements of the AMF model */
  override def elementClient(): ShapesElementClient = new ShapesElementClient(
    _internal.asInstanceOf[InternalShapesConfiguration]
  )

  /** Contains methods to get information about the current state of the configuration */
  def configurationState(): AMLConfigurationState = new AMLConfigurationState(_internal.configurationState())

  /** Set [[ParsingOptions]]
    * @param parsingOptions
    *   [[ParsingOptions]] to add to configuration object
    * @return
    *   [[JsonSchemaBasedSpecConfiguration]] with [[ParsingOptions]] added
    */
  override def withParsingOptions(parsingOptions: ParsingOptions): JsonSchemaBasedSpecConfiguration =
    _internal.withParsingOptions(parsingOptions)

  /** Set [[RenderOptions]]
    * @param renderOptions
    *   [[RenderOptions]] to add to configuration object
    * @return
    *   [[JsonSchemaBasedSpecConfiguration]] with [[ParsingOptions]] added
    */
  override def withRenderOptions(renderOptions: RenderOptions): JsonSchemaBasedSpecConfiguration =
    _internal.withRenderOptions(renderOptions)

  /** Set [[ErrorHandlerProvider]]
    * @param provider
    *   [[ErrorHandlerProvider]] to set to configuration object
    * @return
    *   [[JsonSchemaBasedSpecConfiguration]] with [[ErrorHandlerProvider]] set
    */
  override def withErrorHandlerProvider(provider: ErrorHandlerProvider): JsonSchemaBasedSpecConfiguration =
    _internal.withErrorHandlerProvider(() => provider.errorHandler())

  /** Add a [[ResourceLoader]]
    * @param rl
    *   [[ResourceLoader]] to add to configuration object
    * @return
    *   [[JsonSchemaBasedSpecConfiguration]] with the [[ResourceLoader]] added
    */
  override def withResourceLoader(rl: ResourceLoader): JsonSchemaBasedSpecConfiguration =
    _internal.withResourceLoader(ResourceLoaderMatcher.asInternal(rl))

  /** Set the configuration [[ResourceLoader]]s
    * @param rl
    *   a list of [[ResourceLoader]] to set to the configuration object
    * @return
    *   [[JsonSchemaBasedSpecConfiguration]] with [[ResourceLoader]]s set
    */
  override def withResourceLoaders(rl: ClientList[ResourceLoader]): JsonSchemaBasedSpecConfiguration =
    _internal.withResourceLoaders(rl.asInternal.toList)

  /** Set [[UnitCache]]
    * @param cache
    *   [[UnitCache]] to add to configuration object
    * @return
    *   [[JsonSchemaBasedSpecConfiguration]] with [[UnitCache]] added
    */
  override def withUnitCache(cache: UnitCache): JsonSchemaBasedSpecConfiguration =
    _internal.withUnitCache(UnitCacheMatcher.asInternal(cache))

  /** Add a [[TransformationPipeline]]
    * @param pipeline
    *   [[TransformationPipeline]] to add to configuration object
    * @return
    *   [[JsonSchemaBasedSpecConfiguration]] with [[TransformationPipeline]] added
    */
  override def withTransformationPipeline(pipeline: TransformationPipeline): JsonSchemaBasedSpecConfiguration =
    _internal.withTransformationPipeline(pipeline)

  /** Add an [[AMFEventListener]]
    * @param listener
    *   [[AMFEventListener]] to add to configuration object
    * @return
    *   [[JsonSchemaBasedSpecConfiguration]] with [[AMFEventListener]] added
    */
  override def withEventListener(listener: AMFEventListener): JsonSchemaBasedSpecConfiguration =
    _internal.withEventListener(listener)

  /** Register a Dialect
    * @param dialect
    *   [[Dialect]] to register
    * @return
    *   [[JsonSchemaBasedSpecConfiguration]] with [[Dialect]] registered
    */
  override def withDialect(dialect: Dialect): JsonSchemaBasedSpecConfiguration = _internal.withDialect(dialect)

  /** Register a Dialect
    * @param url
    *   URL of the Dialect to register
    * @return
    *   A CompletableFuture of [[JsonSchemaBasedSpecConfiguration]]
    */
  def withDialect(url: String): ClientFuture[JsonSchemaBasedSpecConfiguration] = _internal.withDialect(url).asClient

  /** Set [[BaseExecutionEnvironment]]
    * @param executionEnv
    *   [[BaseExecutionEnvironment]] to set to configuration object
    * @return
    *   [[JsonSchemaBasedSpecConfiguration]] with [[BaseExecutionEnvironment]] set
    */
  override def withExecutionEnvironment(executionEnv: BaseExecutionEnvironment): JsonSchemaBasedSpecConfiguration =
    _internal.withExecutionEnvironment(executionEnv._internal)

  /** Register a [[Dialect]] linked from a [[DialectInstance]]
    * @param url
    *   of the [[DialectInstance]]
    * @return
    *   A CompletableFuture of [[JsonSchemaBasedSpecConfiguration]]
    */
  def forInstance(url: String): ClientFuture[JsonSchemaBasedSpecConfiguration] = _internal.forInstance(url).asClient

  override def withShapePayloadPlugin(plugin: AMFShapePayloadValidationPlugin): JsonSchemaBasedSpecConfiguration =
    _internal.withPlugin(PayloadValidationPluginMatcher.asInternal(plugin))

  override def withIdAdopterProvider(idAdopterProvider: IdAdopterProvider): JsonSchemaBasedSpecConfiguration =
    _internal.withIdAdopterProvider(idAdopterProvider)
}
