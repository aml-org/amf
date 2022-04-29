package amf.shapes.client.platform.config

import amf.aml.client.platform.AMLConfigurationState
import amf.aml.client.platform.model.document.{Dialect, DialectInstance}
import amf.aml.internal.convert.VocabulariesClientConverter.{ClientFuture, ClientList}
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
import amf.shapes.client.platform.{BaseShapesConfiguration, ShapesConfiguration}
import amf.shapes.client.scala.config.{
  SemanticBaseUnitClient => InternalSemanticBaseUnitClient,
  SemanticJsonSchemaConfiguration => InternalSemanticJsonSchemaConfiguration
}
import amf.shapes.internal.convert.ShapeClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
class SemanticJsonSchemaConfiguration private[amf] (
    private[amf] override val _internal: InternalSemanticJsonSchemaConfiguration
) extends BaseShapesConfiguration(_internal) {

  /** Contains common AMF graph operations associated to documents */
  override def baseUnitClient(): SemanticBaseUnitClient =
    new SemanticBaseUnitClient(new InternalSemanticBaseUnitClient(_internal))

  /** Contains methods to get information about the current state of the configuration */
  def configurationState(): AMLConfigurationState = new AMLConfigurationState(_internal.configurationState())

  /** Set [[ParsingOptions]]
    * @param parsingOptions
    *   [[ParsingOptions]] to add to configuration object
    * @return
    *   [[ShapesConfiguration]] with [[ParsingOptions]] added
    */
  override def withParsingOptions(parsingOptions: ParsingOptions): SemanticJsonSchemaConfiguration =
    _internal.withParsingOptions(parsingOptions)

  /** Set [[RenderOptions]]
    * @param renderOptions
    *   [[RenderOptions]] to add to configuration object
    * @return
    *   [[ShapesConfiguration]] with [[ParsingOptions]] added
    */
  override def withRenderOptions(renderOptions: RenderOptions): SemanticJsonSchemaConfiguration =
    _internal.withRenderOptions(renderOptions)

  /** Set [[ErrorHandlerProvider]]
    * @param provider
    *   [[ErrorHandlerProvider]] to set to configuration object
    * @return
    *   [[ShapesConfiguration]] with [[ErrorHandlerProvider]] set
    */
  override def withErrorHandlerProvider(provider: ErrorHandlerProvider): SemanticJsonSchemaConfiguration =
    _internal.withErrorHandlerProvider(() => provider.errorHandler())

  /** Add a [[ResourceLoader]]
    * @param rl
    *   [[ResourceLoader]] to add to configuration object
    * @return
    *   [[ShapesConfiguration]] with the [[ResourceLoader]] added
    */
  override def withResourceLoader(rl: ResourceLoader): SemanticJsonSchemaConfiguration =
    _internal.withResourceLoader(ResourceLoaderMatcher.asInternal(rl))

  /** Set the configuration [[ResourceLoader]]s
    * @param rl
    *   a list of [[ResourceLoader]] to set to the configuration object
    * @return
    *   [[ShapesConfiguration]] with [[ResourceLoader]]s set
    */
  override def withResourceLoaders(rl: ClientList[ResourceLoader]): SemanticJsonSchemaConfiguration =
    _internal.withResourceLoaders(rl.asInternal.toList)

  /** Set [[UnitCache]]
    * @param cache
    *   [[UnitCache]] to add to configuration object
    * @return
    *   [[ShapesConfiguration]] with [[UnitCache]] added
    */
  override def withUnitCache(cache: UnitCache): SemanticJsonSchemaConfiguration =
    _internal.withUnitCache(UnitCacheMatcher.asInternal(cache))

  /** Add a [[TransformationPipeline]]
    * @param pipeline
    *   [[TransformationPipeline]] to add to configuration object
    * @return
    *   [[ShapesConfiguration]] with [[TransformationPipeline]] added
    */
  override def withTransformationPipeline(pipeline: TransformationPipeline): SemanticJsonSchemaConfiguration =
    _internal.withTransformationPipeline(pipeline)

  /** Add an [[AMFEventListener]]
    * @param listener
    *   [[AMFEventListener]] to add to configuration object
    * @return
    *   [[SemanticJsonSchemaConfiguration]] with [[AMFEventListener]] added
    */
  override def withEventListener(listener: AMFEventListener): SemanticJsonSchemaConfiguration =
    _internal.withEventListener(listener)

  /** Register a Dialect
    * @param dialect
    *   [[Dialect]] to register
    * @return
    *   [[SemanticJsonSchemaConfiguration]] with [[Dialect]] registered
    */
  override def withDialect(dialect: Dialect): SemanticJsonSchemaConfiguration = _internal.withDialect(dialect)

  /** Register a Dialect
    * @param url
    *   URL of the Dialect to register
    * @return
    *   A CompletableFuture of [[SemanticJsonSchemaConfiguration]]
    */
  def withDialect(url: String): ClientFuture[SemanticJsonSchemaConfiguration] = _internal.withDialect(url).asClient

  /** Set [[BaseExecutionEnvironment]]
    * @param executionEnv
    *   [[BaseExecutionEnvironment]] to set to configuration object
    * @return
    *   [[ShapesConfiguration]] with [[BaseExecutionEnvironment]] set
    */
  override def withExecutionEnvironment(executionEnv: BaseExecutionEnvironment): SemanticJsonSchemaConfiguration =
    _internal.withExecutionEnvironment(executionEnv._internal)

  /** Register a [[Dialect]] linked from a [[DialectInstance]]
    * @param url
    *   of the [[DialectInstance]]
    * @return
    *   A CompletableFuture of [[SemanticJsonSchemaConfiguration]]
    */
  def forInstance(url: String): ClientFuture[SemanticJsonSchemaConfiguration] = _internal.forInstance(url).asClient

  override def withShapePayloadPlugin(plugin: AMFShapePayloadValidationPlugin): SemanticJsonSchemaConfiguration =
    _internal.withPlugin(PayloadValidationPluginMatcher.asInternal(plugin))
}

@JSExportAll
@JSExportTopLevel("SemanticJsonSchemaConfiguration")
object SemanticJsonSchemaConfiguration {

  def empty(): SemanticJsonSchemaConfiguration = InternalSemanticJsonSchemaConfiguration.empty()

  /** Predefined environment to deal with AML documents based on ShapesConfiguration predefined() method */
  def predefined(): SemanticJsonSchemaConfiguration = InternalSemanticJsonSchemaConfiguration.predefined()
}
