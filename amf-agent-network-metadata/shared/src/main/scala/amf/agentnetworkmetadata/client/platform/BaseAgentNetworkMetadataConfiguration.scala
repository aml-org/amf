package amf.agentnetworkmetadata.client.platform

import amf.aml.client.platform.model.document.Dialect
import amf.aml.internal.convert.VocabulariesClientConverter.{ClientList, _}
import amf.core.client.platform.config.{AMFEventListener, ParsingOptions, RenderOptions}
import amf.core.client.platform.errorhandling.ErrorHandlerProvider
import amf.core.client.platform.reference.UnitCache
import amf.core.client.platform.resource.ResourceLoader
import amf.core.client.platform.transform.TransformationPipeline
import amf.core.internal.convert.ClientErrorHandlerConverter._
import amf.core.internal.convert.TransformationPipelineConverter._
import amf.agentnetworkmetadata.client.scala.{AgentNetworkMetadataConfiguration => InternalAgentNetworkMetadataConfiguration}
import amf.shapes.client.platform.config.BaseJsonSchemaBasedSpecConfiguration

import scala.concurrent.ExecutionContext
import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class BaseAgentNetworkMetadataConfiguration private[amf](private[amf] override val _internal: InternalAgentNetworkMetadataConfiguration)
    extends BaseJsonSchemaBasedSpecConfiguration(_internal) {

  override protected implicit val ec: ExecutionContext = _internal.getExecutionContext

  override def withParsingOptions(parsingOptions: ParsingOptions): BaseAgentNetworkMetadataConfiguration =
    new BaseAgentNetworkMetadataConfiguration(_internal.withParsingOptions(parsingOptions))

  override def withRenderOptions(renderOptions: RenderOptions): BaseAgentNetworkMetadataConfiguration =
    new BaseAgentNetworkMetadataConfiguration(_internal.withRenderOptions(renderOptions))

  override def withErrorHandlerProvider(provider: ErrorHandlerProvider): BaseAgentNetworkMetadataConfiguration =
    new BaseAgentNetworkMetadataConfiguration(_internal.withErrorHandlerProvider(() => provider.errorHandler()))

  override def withResourceLoader(rl: ResourceLoader): BaseAgentNetworkMetadataConfiguration =
    new BaseAgentNetworkMetadataConfiguration(_internal.withResourceLoader(ResourceLoaderMatcher.asInternal(rl)))

  override def withResourceLoaders(rl: ClientList[ResourceLoader]): BaseAgentNetworkMetadataConfiguration =
    new BaseAgentNetworkMetadataConfiguration(_internal.withResourceLoaders(rl.asInternal.toList))

  override def withUnitCache(cache: UnitCache): BaseAgentNetworkMetadataConfiguration =
    new BaseAgentNetworkMetadataConfiguration(_internal.withUnitCache(UnitCacheMatcher.asInternal(cache)))

  override def withTransformationPipeline(pipeline: TransformationPipeline): BaseAgentNetworkMetadataConfiguration =
    new BaseAgentNetworkMetadataConfiguration(_internal.withTransformationPipeline(pipeline))

  override def withEventListener(listener: AMFEventListener): BaseAgentNetworkMetadataConfiguration =
    new BaseAgentNetworkMetadataConfiguration(_internal.withEventListener(listener))

  override def withDialect(dialect: Dialect): BaseAgentNetworkMetadataConfiguration =
    new BaseAgentNetworkMetadataConfiguration(_internal.withDialect(dialect))
}
