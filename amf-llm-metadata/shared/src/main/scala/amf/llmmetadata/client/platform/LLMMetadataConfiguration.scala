package amf.llmmetadata.client.platform

import amf.aml.client.platform.model.document.{Dialect, DialectInstance}
import amf.aml.client.platform.{AMLBaseUnitClient, AMLConfigurationState}
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
import amf.llmmetadata.client.scala.{
  LLMMetadataBaseUnitClient => InternalLLMMetadataBaseUnitClient,
  LLMMetadataConfiguration => InternalLLMMetadataConfiguration
}
import amf.llmmetadata.internal.convert.LLMMetadataClientConverters._
import amf.shapes.client.platform.ShapesElementClient
import amf.shapes.client.scala.{ShapesConfiguration => InternalShapesConfiguration}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
class LLMMetadataConfiguration private[amf] (private[amf] override val _internal: InternalLLMMetadataConfiguration)
    extends BaseLLMMetadataConfiguration(_internal) {

  /** Contains common AMF graph operations associated to documents */
  override def baseUnitClient(): AMLBaseUnitClient = new LLMMetadataBaseUnitClient(new InternalLLMMetadataBaseUnitClient(_internal))

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
    *   [[LLMMetadataConfiguration]] with [[ParsingOptions]] added
    */
  override def withParsingOptions(parsingOptions: ParsingOptions): LLMMetadataConfiguration =
    _internal.withParsingOptions(parsingOptions)

  /** Set [[RenderOptions]]
    * @param renderOptions
    *   [[RenderOptions]] to add to configuration object
    * @return
    *   [[LLMMetadataConfiguration]] with [[ParsingOptions]] added
    */
  override def withRenderOptions(renderOptions: RenderOptions): LLMMetadataConfiguration =
    _internal.withRenderOptions(renderOptions)

  /** Set [[ErrorHandlerProvider]]
    * @param provider
    *   [[ErrorHandlerProvider]] to set to configuration object
    * @return
    *   [[LLMMetadataConfiguration]] with [[ErrorHandlerProvider]] set
    */
  override def withErrorHandlerProvider(provider: ErrorHandlerProvider): LLMMetadataConfiguration =
    _internal.withErrorHandlerProvider(() => provider.errorHandler())

  /** Add a [[ResourceLoader]]
    * @param rl
    *   [[ResourceLoader]] to add to configuration object
    * @return
    *   [[LLMMetadataConfiguration]] with the [[ResourceLoader]] added
    */
  override def withResourceLoader(rl: ResourceLoader): LLMMetadataConfiguration =
    _internal.withResourceLoader(ResourceLoaderMatcher.asInternal(rl))

  /** Set the configuration [[ResourceLoader]]s
    * @param rl
    *   a list of [[ResourceLoader]] to set to the configuration object
    * @return
    *   [[LLMMetadataConfiguration]] with [[ResourceLoader]]s set
    */
  override def withResourceLoaders(rl: ClientList[ResourceLoader]): LLMMetadataConfiguration =
    _internal.withResourceLoaders(rl.asInternal.toList)

  /** Set [[UnitCache]]
    * @param cache
    *   [[UnitCache]] to add to configuration object
    * @return
    *   [[LLMMetadataConfiguration]] with [[UnitCache]] added
    */
  override def withUnitCache(cache: UnitCache): LLMMetadataConfiguration =
    _internal.withUnitCache(UnitCacheMatcher.asInternal(cache))

  /** Add a [[TransformationPipeline]]
    * @param pipeline
    *   [[TransformationPipeline]] to add to configuration object
    * @return
    *   [[LLMMetadataConfiguration]] with [[TransformationPipeline]] added
    */
  override def withTransformationPipeline(pipeline: TransformationPipeline): LLMMetadataConfiguration =
    _internal.withTransformationPipeline(pipeline)

  /** Add an [[AMFEventListener]]
    * @param listener
    *   [[AMFEventListener]] to add to configuration object
    * @return
    *   [[LLMMetadataConfiguration]] with [[AMFEventListener]] added
    */
  override def withEventListener(listener: AMFEventListener): LLMMetadataConfiguration =
    _internal.withEventListener(listener)

  /** Register a Dialect
    * @param dialect
    *   [[Dialect]] to register
    * @return
    *   [[LLMMetadataConfiguration]] with [[Dialect]] registered
    */
  override def withDialect(dialect: Dialect): LLMMetadataConfiguration = _internal.withDialect(dialect)

  /** Register a Dialect
    * @param url
    *   URL of the Dialect to register
    * @return
    *   A CompletableFuture of [[LLMMetadataConfiguration]]
    */
  def withDialect(url: String): ClientFuture[LLMMetadataConfiguration] = _internal.withDialect(url).asClient

  /** Set [[BaseExecutionEnvironment]]
    * @param executionEnv
    *   [[BaseExecutionEnvironment]] to set to configuration object
    * @return
    *   [[LLMMetadataConfiguration]] with [[BaseExecutionEnvironment]] set
    */
  override def withExecutionEnvironment(executionEnv: BaseExecutionEnvironment): LLMMetadataConfiguration =
    _internal.withExecutionEnvironment(executionEnv._internal)

  /** Register a [[Dialect]] linked from a [[DialectInstance]]
    * @param url
    *   of the [[DialectInstance]]
    * @return
    *   A CompletableFuture of [[LLMMetadataConfiguration]]
    */
  def forInstance(url: String): ClientFuture[LLMMetadataConfiguration] = _internal.forInstance(url).asClient

  override def withShapePayloadPlugin(plugin: AMFShapePayloadValidationPlugin): LLMMetadataConfiguration =
    _internal.withPlugin(PayloadValidationPluginMatcher.asInternal(plugin))

  override def withIdAdopterProvider(idAdopterProvider: IdAdopterProvider): LLMMetadataConfiguration =
    _internal.withIdAdopterProvider(idAdopterProvider)
}

@JSExportAll
@JSExportTopLevel("LLMMetadataConfiguration")
object LLMMetadataConfiguration {

  def LLMMetadata(): LLMMetadataConfiguration = new LLMMetadataConfiguration(InternalLLMMetadataConfiguration.LLMMetadata())

}
