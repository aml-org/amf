package amf.agentmetadata.client.platform

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
import amf.agentmetadata.client.scala.{
  AgentMetadataBaseUnitClient => InternalAgentMetadataBaseUnitClient,
  AgentMetadataConfiguration => InternalAgentMetadataConfiguration
}
import amf.agentmetadata.internal.convert.AgentMetadataClientConverters._
import amf.shapes.client.platform.ShapesElementClient
import amf.shapes.client.scala.{ShapesConfiguration => InternalShapesConfiguration}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
class AgentMetadataConfiguration private[amf] (private[amf] override val _internal: InternalAgentMetadataConfiguration)
    extends BaseAgentMetadataConfiguration(_internal) {

  /** Contains common AMF graph operations associated to documents */
  override def baseUnitClient(): AMLBaseUnitClient = new AgentMetadataBaseUnitClient(new InternalAgentMetadataBaseUnitClient(_internal))

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
    *   [[AgentMetadataConfiguration]] with [[ParsingOptions]] added
    */
  override def withParsingOptions(parsingOptions: ParsingOptions): AgentMetadataConfiguration =
    _internal.withParsingOptions(parsingOptions)

  /** Set [[RenderOptions]]
    * @param renderOptions
    *   [[RenderOptions]] to add to configuration object
    * @return
    *   [[AgentMetadataConfiguration]] with [[ParsingOptions]] added
    */
  override def withRenderOptions(renderOptions: RenderOptions): AgentMetadataConfiguration =
    _internal.withRenderOptions(renderOptions)

  /** Set [[ErrorHandlerProvider]]
    * @param provider
    *   [[ErrorHandlerProvider]] to set to configuration object
    * @return
    *   [[AgentMetadataConfiguration]] with [[ErrorHandlerProvider]] set
    */
  override def withErrorHandlerProvider(provider: ErrorHandlerProvider): AgentMetadataConfiguration =
    _internal.withErrorHandlerProvider(() => provider.errorHandler())

  /** Add a [[ResourceLoader]]
    * @param rl
    *   [[ResourceLoader]] to add to configuration object
    * @return
    *   [[AgentMetadataConfiguration]] with the [[ResourceLoader]] added
    */
  override def withResourceLoader(rl: ResourceLoader): AgentMetadataConfiguration =
    _internal.withResourceLoader(ResourceLoaderMatcher.asInternal(rl))

  /** Set the configuration [[ResourceLoader]]s
    * @param rl
    *   a list of [[ResourceLoader]] to set to the configuration object
    * @return
    *   [[AgentMetadataConfiguration]] with [[ResourceLoader]]s set
    */
  override def withResourceLoaders(rl: ClientList[ResourceLoader]): AgentMetadataConfiguration =
    _internal.withResourceLoaders(rl.asInternal.toList)

  /** Set [[UnitCache]]
    * @param cache
    *   [[UnitCache]] to add to configuration object
    * @return
    *   [[AgentMetadataConfiguration]] with [[UnitCache]] added
    */
  override def withUnitCache(cache: UnitCache): AgentMetadataConfiguration =
    _internal.withUnitCache(UnitCacheMatcher.asInternal(cache))

  /** Add a [[TransformationPipeline]]
    * @param pipeline
    *   [[TransformationPipeline]] to add to configuration object
    * @return
    *   [[AgentMetadataConfiguration]] with [[TransformationPipeline]] added
    */
  override def withTransformationPipeline(pipeline: TransformationPipeline): AgentMetadataConfiguration =
    _internal.withTransformationPipeline(pipeline)

  /** Add an [[AMFEventListener]]
    * @param listener
    *   [[AMFEventListener]] to add to configuration object
    * @return
    *   [[AgentMetadataConfiguration]] with [[AMFEventListener]] added
    */
  override def withEventListener(listener: AMFEventListener): AgentMetadataConfiguration =
    _internal.withEventListener(listener)

  /** Register a Dialect
    * @param dialect
    *   [[Dialect]] to register
    * @return
    *   [[AgentMetadataConfiguration]] with [[Dialect]] registered
    */
  override def withDialect(dialect: Dialect): AgentMetadataConfiguration = _internal.withDialect(dialect)

  /** Register a Dialect
    * @param url
    *   URL of the Dialect to register
    * @return
    *   A CompletableFuture of [[AgentMetadataConfiguration]]
    */
  def withDialect(url: String): ClientFuture[AgentMetadataConfiguration] = _internal.withDialect(url).asClient

  /** Set [[BaseExecutionEnvironment]]
    * @param executionEnv
    *   [[BaseExecutionEnvironment]] to set to configuration object
    * @return
    *   [[AgentMetadataConfiguration]] with [[BaseExecutionEnvironment]] set
    */
  override def withExecutionEnvironment(executionEnv: BaseExecutionEnvironment): AgentMetadataConfiguration =
    _internal.withExecutionEnvironment(executionEnv._internal)

  /** Register a [[Dialect]] linked from a [[DialectInstance]]
    * @param url
    *   of the [[DialectInstance]]
    * @return
    *   A CompletableFuture of [[AgentMetadataConfiguration]]
    */
  def forInstance(url: String): ClientFuture[AgentMetadataConfiguration] = _internal.forInstance(url).asClient

  override def withShapePayloadPlugin(plugin: AMFShapePayloadValidationPlugin): AgentMetadataConfiguration =
    _internal.withPlugin(PayloadValidationPluginMatcher.asInternal(plugin))

  override def withIdAdopterProvider(idAdopterProvider: IdAdopterProvider): AgentMetadataConfiguration =
    _internal.withIdAdopterProvider(idAdopterProvider)
}

@JSExportAll
@JSExportTopLevel("AgentMetadataConfiguration")
object AgentMetadataConfiguration {

  def AgentMetadata(): AgentMetadataConfiguration = new AgentMetadataConfiguration(InternalAgentMetadataConfiguration.AgentMetadata())

}
