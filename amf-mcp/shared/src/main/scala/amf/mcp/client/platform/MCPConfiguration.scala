package amf.mcp.client.platform

import amf.aml.client.platform.model.document.{Dialect, DialectInstance}
import amf.aml.client.platform.{AMLBaseUnitClient, AMLConfigurationState}
import amf.mcp.client.scala.{MCPBaseUnitClient => InternalMCPBaseUnitClient}
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
import amf.mcp.client.scala.{MCPConfiguration => InternalMCPConfiguration}
import amf.shapes.client.scala.{ShapesConfiguration => InternalShapesConfiguration}
import amf.shapes.client.platform.ShapesElementClient
import amf.mcp.internal.convert.MCPClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
class MCPConfiguration private[amf] (private[amf] override val _internal: InternalMCPConfiguration)
    extends BaseMCPConfiguration(_internal) {

  /** Contains common AMF graph operations associated to documents */
  override def baseUnitClient(): AMLBaseUnitClient = new MCPBaseUnitClient(new InternalMCPBaseUnitClient(_internal))

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
    *   [[MCPConfiguration]] with [[ParsingOptions]] added
    */
  override def withParsingOptions(parsingOptions: ParsingOptions): MCPConfiguration =
    _internal.withParsingOptions(parsingOptions)

  /** Set [[RenderOptions]]
    * @param renderOptions
    *   [[RenderOptions]] to add to configuration object
    * @return
    *   [[MCPConfiguration]] with [[ParsingOptions]] added
    */
  override def withRenderOptions(renderOptions: RenderOptions): MCPConfiguration =
    _internal.withRenderOptions(renderOptions)

  /** Set [[ErrorHandlerProvider]]
    * @param provider
    *   [[ErrorHandlerProvider]] to set to configuration object
    * @return
    *   [[MCPConfiguration]] with [[ErrorHandlerProvider]] set
    */
  override def withErrorHandlerProvider(provider: ErrorHandlerProvider): MCPConfiguration =
    _internal.withErrorHandlerProvider(() => provider.errorHandler())

  /** Add a [[ResourceLoader]]
    * @param rl
    *   [[ResourceLoader]] to add to configuration object
    * @return
    *   [[MCPConfiguration]] with the [[ResourceLoader]] added
    */
  override def withResourceLoader(rl: ResourceLoader): MCPConfiguration =
    _internal.withResourceLoader(ResourceLoaderMatcher.asInternal(rl))

  /** Set the configuration [[ResourceLoader]]s
    * @param rl
    *   a list of [[ResourceLoader]] to set to the configuration object
    * @return
    *   [[MCPConfiguration]] with [[ResourceLoader]]s set
    */
  override def withResourceLoaders(rl: ClientList[ResourceLoader]): MCPConfiguration =
    _internal.withResourceLoaders(rl.asInternal.toList)

  /** Set [[UnitCache]]
    * @param cache
    *   [[UnitCache]] to add to configuration object
    * @return
    *   [[MCPConfiguration]] with [[UnitCache]] added
    */
  override def withUnitCache(cache: UnitCache): MCPConfiguration =
    _internal.withUnitCache(UnitCacheMatcher.asInternal(cache))

  /** Add a [[TransformationPipeline]]
    * @param pipeline
    *   [[TransformationPipeline]] to add to configuration object
    * @return
    *   [[MCPConfiguration]] with [[TransformationPipeline]] added
    */
  override def withTransformationPipeline(pipeline: TransformationPipeline): MCPConfiguration =
    _internal.withTransformationPipeline(pipeline)

  /** Add an [[AMFEventListener]]
    * @param listener
    *   [[AMFEventListener]] to add to configuration object
    * @return
    *   [[MCPConfiguration]] with [[AMFEventListener]] added
    */
  override def withEventListener(listener: AMFEventListener): MCPConfiguration =
    _internal.withEventListener(listener)

  /** Register a Dialect
    * @param dialect
    *   [[Dialect]] to register
    * @return
    *   [[MCPConfiguration]] with [[Dialect]] registered
    */
  override def withDialect(dialect: Dialect): MCPConfiguration = _internal.withDialect(dialect)

  /** Register a Dialect
    * @param url
    *   URL of the Dialect to register
    * @return
    *   A CompletableFuture of [[MCPConfiguration]]
    */
  def withDialect(url: String): ClientFuture[MCPConfiguration] = _internal.withDialect(url).asClient

  /** Set [[BaseExecutionEnvironment]]
    * @param executionEnv
    *   [[BaseExecutionEnvironment]] to set to configuration object
    * @return
    *   [[MCPConfiguration]] with [[BaseExecutionEnvironment]] set
    */
  override def withExecutionEnvironment(executionEnv: BaseExecutionEnvironment): MCPConfiguration =
    _internal.withExecutionEnvironment(executionEnv._internal)

  /** Register a [[Dialect]] linked from a [[DialectInstance]]
    * @param url
    *   of the [[DialectInstance]]
    * @return
    *   A CompletableFuture of [[MCPConfiguration]]
    */
  def forInstance(url: String): ClientFuture[MCPConfiguration] = _internal.forInstance(url).asClient

  override def withShapePayloadPlugin(plugin: AMFShapePayloadValidationPlugin): MCPConfiguration =
    _internal.withPlugin(PayloadValidationPluginMatcher.asInternal(plugin))

  override def withIdAdopterProvider(idAdopterProvider: IdAdopterProvider): MCPConfiguration =
    _internal.withIdAdopterProvider(idAdopterProvider)
}

@JSExportAll
@JSExportTopLevel("MCPConfiguration")
object MCPConfiguration {

  def MCP(): MCPConfiguration = new MCPConfiguration(InternalMCPConfiguration.MCP())

}
