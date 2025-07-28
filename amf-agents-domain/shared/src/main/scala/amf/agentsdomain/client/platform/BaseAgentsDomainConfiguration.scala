package amf.agentsdomain.client.platform

import amf.aml.client.platform.model.document.Dialect
import amf.aml.internal.convert.VocabulariesClientConverter.{ClientList, _}
import amf.core.client.platform.config.{AMFEventListener, ParsingOptions, RenderOptions}
import amf.core.client.platform.errorhandling.ErrorHandlerProvider
import amf.core.client.platform.reference.UnitCache
import amf.core.client.platform.resource.ResourceLoader
import amf.core.client.platform.transform.TransformationPipeline
import amf.core.internal.convert.ClientErrorHandlerConverter._
import amf.core.internal.convert.TransformationPipelineConverter._
import amf.agentsdomain.client.scala.{AgentsDomainConfiguration => InternalAgentsDomainConfiguration}
import amf.shapes.client.platform.config.BaseJsonSchemaBasedSpecConfiguration

import scala.concurrent.ExecutionContext
import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class BaseAgentsDomainConfiguration private[amf](private[amf] override val _internal: InternalAgentsDomainConfiguration)
    extends BaseJsonSchemaBasedSpecConfiguration(_internal) {

  override protected implicit val ec: ExecutionContext = _internal.getExecutionContext

  override def withParsingOptions(parsingOptions: ParsingOptions): BaseAgentsDomainConfiguration =
    new BaseAgentsDomainConfiguration(_internal.withParsingOptions(parsingOptions))

  override def withRenderOptions(renderOptions: RenderOptions): BaseAgentsDomainConfiguration =
    new BaseAgentsDomainConfiguration(_internal.withRenderOptions(renderOptions))

  override def withErrorHandlerProvider(provider: ErrorHandlerProvider): BaseAgentsDomainConfiguration =
    new BaseAgentsDomainConfiguration(_internal.withErrorHandlerProvider(() => provider.errorHandler()))

  override def withResourceLoader(rl: ResourceLoader): BaseAgentsDomainConfiguration =
    new BaseAgentsDomainConfiguration(_internal.withResourceLoader(ResourceLoaderMatcher.asInternal(rl)))

  override def withResourceLoaders(rl: ClientList[ResourceLoader]): BaseAgentsDomainConfiguration =
    new BaseAgentsDomainConfiguration(_internal.withResourceLoaders(rl.asInternal.toList))

  override def withUnitCache(cache: UnitCache): BaseAgentsDomainConfiguration =
    new BaseAgentsDomainConfiguration(_internal.withUnitCache(UnitCacheMatcher.asInternal(cache)))

  override def withTransformationPipeline(pipeline: TransformationPipeline): BaseAgentsDomainConfiguration =
    new BaseAgentsDomainConfiguration(_internal.withTransformationPipeline(pipeline))

  override def withEventListener(listener: AMFEventListener): BaseAgentsDomainConfiguration =
    new BaseAgentsDomainConfiguration(_internal.withEventListener(listener))

  override def withDialect(dialect: Dialect): BaseAgentsDomainConfiguration =
    new BaseAgentsDomainConfiguration(_internal.withDialect(dialect))
}
