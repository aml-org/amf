package amf.agentgraph.client.platform

import amf.aml.client.platform.model.document.Dialect
import amf.aml.internal.convert.VocabulariesClientConverter.{ClientList, _}
import amf.core.client.platform.config.{AMFEventListener, ParsingOptions, RenderOptions}
import amf.core.client.platform.errorhandling.ErrorHandlerProvider
import amf.core.client.platform.reference.UnitCache
import amf.core.client.platform.resource.ResourceLoader
import amf.core.client.platform.transform.TransformationPipeline
import amf.core.internal.convert.ClientErrorHandlerConverter._
import amf.core.internal.convert.TransformationPipelineConverter._
import amf.agentgraph.client.scala.{AgentGraphConfiguration => InternalAgentGraphConfiguration}
import amf.shapes.client.platform.config.BaseJsonSchemaBasedSpecConfiguration

import scala.concurrent.ExecutionContext
import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class BaseAgentGraphConfiguration private[amf](private[amf] override val _internal: InternalAgentGraphConfiguration)
    extends BaseJsonSchemaBasedSpecConfiguration(_internal) {

  override protected implicit val ec: ExecutionContext = _internal.getExecutionContext

  override def withParsingOptions(parsingOptions: ParsingOptions): BaseAgentGraphConfiguration =
    new BaseAgentGraphConfiguration(_internal.withParsingOptions(parsingOptions))

  override def withRenderOptions(renderOptions: RenderOptions): BaseAgentGraphConfiguration =
    new BaseAgentGraphConfiguration(_internal.withRenderOptions(renderOptions))

  override def withErrorHandlerProvider(provider: ErrorHandlerProvider): BaseAgentGraphConfiguration =
    new BaseAgentGraphConfiguration(_internal.withErrorHandlerProvider(() => provider.errorHandler()))

  override def withResourceLoader(rl: ResourceLoader): BaseAgentGraphConfiguration =
    new BaseAgentGraphConfiguration(_internal.withResourceLoader(ResourceLoaderMatcher.asInternal(rl)))

  override def withResourceLoaders(rl: ClientList[ResourceLoader]): BaseAgentGraphConfiguration =
    new BaseAgentGraphConfiguration(_internal.withResourceLoaders(rl.asInternal.toList))

  override def withUnitCache(cache: UnitCache): BaseAgentGraphConfiguration =
    new BaseAgentGraphConfiguration(_internal.withUnitCache(UnitCacheMatcher.asInternal(cache)))

  override def withTransformationPipeline(pipeline: TransformationPipeline): BaseAgentGraphConfiguration =
    new BaseAgentGraphConfiguration(_internal.withTransformationPipeline(pipeline))

  override def withEventListener(listener: AMFEventListener): BaseAgentGraphConfiguration =
    new BaseAgentGraphConfiguration(_internal.withEventListener(listener))

  override def withDialect(dialect: Dialect): BaseAgentGraphConfiguration =
    new BaseAgentGraphConfiguration(_internal.withDialect(dialect))
}
