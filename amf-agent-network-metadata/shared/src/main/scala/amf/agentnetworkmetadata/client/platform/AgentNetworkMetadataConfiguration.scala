package amf.agentnetworkmetadata.client.platform

import amf.agentnetworkmetadata.client.scala.{
  AgentNetworkMetadataBaseUnitClient => InternalAgentNetworkMetadataBaseUnitClient,
  AgentNetworkMetadataConfiguration => InternalAgentNetworkMetadataConfiguration
}
import amf.agentnetworkmetadata.internal.convert.AgentNetworkMetadataClientConverters._
import amf.aml.client.platform.model.document.Dialect
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
import amf.shapes.client.platform.ShapesElementClient
import amf.shapes.client.scala.{ShapesConfiguration => InternalShapesConfiguration}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
class AgentNetworkMetadataConfiguration private[amf] (
    private[amf] override val _internal: InternalAgentNetworkMetadataConfiguration)
    extends BaseAgentNetworkMetadataConfiguration(_internal) {

  override def baseUnitClient(): AMLBaseUnitClient =
    new AgentNetworkMetadataBaseUnitClient(new InternalAgentNetworkMetadataBaseUnitClient(_internal))

  override def elementClient(): ShapesElementClient = new ShapesElementClient(
      _internal.asInstanceOf[InternalShapesConfiguration]
  )

  def configurationState(): AMLConfigurationState = new AMLConfigurationState(_internal.configurationState())

  override def withParsingOptions(parsingOptions: ParsingOptions): AgentNetworkMetadataConfiguration =
    _internal.withParsingOptions(parsingOptions)

  override def withRenderOptions(renderOptions: RenderOptions): AgentNetworkMetadataConfiguration =
    _internal.withRenderOptions(renderOptions)

  override def withErrorHandlerProvider(provider: ErrorHandlerProvider): AgentNetworkMetadataConfiguration =
    _internal.withErrorHandlerProvider(() => provider.errorHandler())

  override def withResourceLoader(rl: ResourceLoader): AgentNetworkMetadataConfiguration =
    _internal.withResourceLoader(ResourceLoaderMatcher.asInternal(rl))

  override def withResourceLoaders(rl: ClientList[ResourceLoader]): AgentNetworkMetadataConfiguration =
    _internal.withResourceLoaders(rl.asInternal.toList)

  override def withUnitCache(cache: UnitCache): AgentNetworkMetadataConfiguration =
    _internal.withUnitCache(UnitCacheMatcher.asInternal(cache))

  override def withTransformationPipeline(pipeline: TransformationPipeline): AgentNetworkMetadataConfiguration =
    _internal.withTransformationPipeline(pipeline)

  override def withEventListener(listener: AMFEventListener): AgentNetworkMetadataConfiguration =
    _internal.withEventListener(listener)

  override def withDialect(dialect: Dialect): AgentNetworkMetadataConfiguration = _internal.withDialect(dialect)

  def withDialect(url: String): ClientFuture[AgentNetworkMetadataConfiguration] = _internal.withDialect(url).asClient

  override def withExecutionEnvironment(executionEnv: BaseExecutionEnvironment): AgentNetworkMetadataConfiguration =
    _internal.withExecutionEnvironment(executionEnv._internal)

  def forInstance(url: String): ClientFuture[AgentNetworkMetadataConfiguration] = _internal.forInstance(url).asClient

  override def withShapePayloadPlugin(plugin: AMFShapePayloadValidationPlugin): AgentNetworkMetadataConfiguration =
    _internal.withPlugin(PayloadValidationPluginMatcher.asInternal(plugin))

  override def withIdAdopterProvider(idAdopterProvider: IdAdopterProvider): AgentNetworkMetadataConfiguration =
    _internal.withIdAdopterProvider(idAdopterProvider)
}

@JSExportAll
@JSExportTopLevel("AgentNetworkMetadataConfiguration")
object AgentNetworkMetadataConfiguration {

  def AgentNetworkMetadata(): AgentNetworkMetadataConfiguration =
    new AgentNetworkMetadataConfiguration(InternalAgentNetworkMetadataConfiguration.AgentNetworkMetadata())

}
