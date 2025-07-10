package amf.agentfabric.client.platform

import amf.aml.client.platform.model.document.Dialect
import amf.aml.internal.convert.VocabulariesClientConverter.{ClientList, _}
import amf.core.client.platform.config.{AMFEventListener, ParsingOptions, RenderOptions}
import amf.core.client.platform.errorhandling.ErrorHandlerProvider
import amf.core.client.platform.reference.UnitCache
import amf.core.client.platform.resource.ResourceLoader
import amf.core.client.platform.transform.TransformationPipeline
import amf.core.internal.convert.ClientErrorHandlerConverter._
import amf.core.internal.convert.TransformationPipelineConverter._
import amf.agentfabric.client.scala.{AgentFabricConfiguration => InternalAgentFabricConfiguration}
import amf.shapes.client.platform.config.BaseJsonSchemaBasedSpecConfiguration

import scala.concurrent.ExecutionContext
import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class BaseAgentFabricConfiguration private[amf](private[amf] override val _internal: InternalAgentFabricConfiguration)
    extends BaseJsonSchemaBasedSpecConfiguration(_internal) {

  override protected implicit val ec: ExecutionContext = _internal.getExecutionContext

  override def withParsingOptions(parsingOptions: ParsingOptions): BaseAgentFabricConfiguration =
    new BaseAgentFabricConfiguration(_internal.withParsingOptions(parsingOptions))

  override def withRenderOptions(renderOptions: RenderOptions): BaseAgentFabricConfiguration =
    new BaseAgentFabricConfiguration(_internal.withRenderOptions(renderOptions))

  override def withErrorHandlerProvider(provider: ErrorHandlerProvider): BaseAgentFabricConfiguration =
    new BaseAgentFabricConfiguration(_internal.withErrorHandlerProvider(() => provider.errorHandler()))

  override def withResourceLoader(rl: ResourceLoader): BaseAgentFabricConfiguration =
    new BaseAgentFabricConfiguration(_internal.withResourceLoader(ResourceLoaderMatcher.asInternal(rl)))

  override def withResourceLoaders(rl: ClientList[ResourceLoader]): BaseAgentFabricConfiguration =
    new BaseAgentFabricConfiguration(_internal.withResourceLoaders(rl.asInternal.toList))

  override def withUnitCache(cache: UnitCache): BaseAgentFabricConfiguration =
    new BaseAgentFabricConfiguration(_internal.withUnitCache(UnitCacheMatcher.asInternal(cache)))

  override def withTransformationPipeline(pipeline: TransformationPipeline): BaseAgentFabricConfiguration =
    new BaseAgentFabricConfiguration(_internal.withTransformationPipeline(pipeline))

  override def withEventListener(listener: AMFEventListener): BaseAgentFabricConfiguration =
    new BaseAgentFabricConfiguration(_internal.withEventListener(listener))

  override def withDialect(dialect: Dialect): BaseAgentFabricConfiguration =
    new BaseAgentFabricConfiguration(_internal.withDialect(dialect))
}
