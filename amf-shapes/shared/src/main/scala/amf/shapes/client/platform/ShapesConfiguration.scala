package amf.shapes.client.platform

import amf.aml.client.platform.model.document.Dialect
import amf.aml.client.platform.model.document.DialectInstance
import amf.aml.client.platform.{AMLBaseUnitClient, AMLConfigurationState}
import amf.aml.client.scala.{AMLBaseUnitClient => InternalAMLBaseUnitClient}
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
import amf.shapes.client.scala.{ShapesConfiguration => InternalShapesConfiguration}
import amf.shapes.internal.convert.ShapeClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
class ShapesConfiguration private[amf] (private[amf] override val _internal: InternalShapesConfiguration)
    extends BaseShapesConfiguration(_internal) {

  /** Contains common AMF graph operations associated to documents */
  override def baseUnitClient(): AMLBaseUnitClient = new AMLBaseUnitClient(new InternalAMLBaseUnitClient(_internal))

  /** Contains functionality associated with specific elements of the AMF model */
  override def elementClient(): ShapesElementClient = new ShapesElementClient(this)

  /** Contains methods to get information about the current state of the configuration */
  def configurationState(): AMLConfigurationState = new AMLConfigurationState(_internal.configurationState())

  /**
    * Set [[ParsingOptions]]
    * @param parsingOptions [[ParsingOptions]] to add to configuration object
    * @return [[ShapesConfiguration]] with [[ParsingOptions]] added
    */
  override def withParsingOptions(parsingOptions: ParsingOptions): ShapesConfiguration =
    _internal.withParsingOptions(parsingOptions)

  /**
    * Set [[RenderOptions]]
    * @param renderOptions [[RenderOptions]] to add to configuration object
    * @return [[ShapesConfiguration]] with [[ParsingOptions]] added
    */
  override def withRenderOptions(renderOptions: RenderOptions): ShapesConfiguration =
    _internal.withRenderOptions(renderOptions)

  /**
    * Set [[ErrorHandlerProvider]]
    * @param provider [[ErrorHandlerProvider]] to set to configuration object
    * @return [[ShapesConfiguration]] with [[ErrorHandlerProvider]] set
    */
  override def withErrorHandlerProvider(provider: ErrorHandlerProvider): ShapesConfiguration =
    _internal.withErrorHandlerProvider(() => provider.errorHandler())

  /**
    * Add a [[ResourceLoader]]
    * @param rl [[ResourceLoader]] to add to configuration object
    * @return [[ShapesConfiguration]] with the [[ResourceLoader]] added
    */
  override def withResourceLoader(rl: ResourceLoader): ShapesConfiguration =
    _internal.withResourceLoader(ResourceLoaderMatcher.asInternal(rl))

  /**
    * Set the configuration [[ResourceLoader]]s
    * @param rl a list of [[ResourceLoader]] to set to the configuration object
    * @return [[ShapesConfiguration]] with [[ResourceLoader]]s set
    */
  override def withResourceLoaders(rl: ClientList[ResourceLoader]): ShapesConfiguration =
    _internal.withResourceLoaders(rl.asInternal.toList)

  /**
    * Set [[UnitCache]]
    * @param cache [[UnitCache]] to add to configuration object
    * @return [[ShapesConfiguration]] with [[UnitCache]] added
    */
  override def withUnitCache(cache: UnitCache): ShapesConfiguration =
    _internal.withUnitCache(UnitCacheMatcher.asInternal(cache))

  /**
    * Add a [[TransformationPipeline]]
    * @param pipeline [[TransformationPipeline]] to add to configuration object
    * @return [[ShapesConfiguration]] with [[TransformationPipeline]] added
    */
  override def withTransformationPipeline(pipeline: TransformationPipeline): ShapesConfiguration =
    _internal.withTransformationPipeline(pipeline)

  /**
    * Add an [[AMFEventListener]]
    * @param listener [[AMFEventListener]] to add to configuration object
    * @return [[ShapesConfiguration]] with [[AMFEventListener]] added
    */
  override def withEventListener(listener: AMFEventListener): ShapesConfiguration =
    _internal.withEventListener(listener)

  /**
    * Register a Dialect
    * @param dialect [[Dialect]] to register
    * @return [[ShapesConfiguration]] with [[Dialect]] registered
    */
  override def withDialect(dialect: Dialect): ShapesConfiguration = _internal.withDialect(dialect)

  /**
    * Register a Dialect
    * @param url URL of the Dialect to register
    * @return A CompletableFuture of [[ShapesConfiguration]]
    */
  def withDialect(url: String): ClientFuture[ShapesConfiguration] = _internal.withDialect(url).asClient

  /**
    * Set [[BaseExecutionEnvironment]]
    * @param executionEnv [[BaseExecutionEnvironment]] to set to configuration object
    * @return [[ShapesConfiguration]] with [[BaseExecutionEnvironment]] set
    */
  override def withExecutionEnvironment(executionEnv: BaseExecutionEnvironment): ShapesConfiguration =
    _internal.withExecutionEnvironment(executionEnv._internal)

  /**
    * Register a [[Dialect]] linked from a [[DialectInstance]]
    * @param url of the [[DialectInstance]]
    * @return A CompletableFuture of [[ShapesConfiguration]]
    */
  def forInstance(url: String): ClientFuture[ShapesConfiguration] = _internal.forInstance(url).asClient

  override def withShapePayloadPlugin(plugin: AMFShapePayloadValidationPlugin): ShapesConfiguration =
    _internal.withPlugin(PayloadValidationPluginMatcher.asInternal(plugin))
}

@JSExportAll
@JSExportTopLevel("ShapesConfiguration")
object ShapesConfiguration {

  def empty(): ShapesConfiguration = InternalShapesConfiguration.empty()

  /** Predefined environment to deal with AML documents based on ShapesConfiguration predefined() method */
  def predefined(): ShapesConfiguration = InternalShapesConfiguration.predefined()
}
