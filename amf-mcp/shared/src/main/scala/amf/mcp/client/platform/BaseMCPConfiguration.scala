package amf.mcp.client.platform

import amf.aml.client.platform.model.document.Dialect
import amf.aml.internal.convert.VocabulariesClientConverter.{ClientList, _}
import amf.core.client.platform.config.{AMFEventListener, ParsingOptions, RenderOptions}
import amf.core.client.platform.errorhandling.ErrorHandlerProvider
import amf.core.client.platform.reference.UnitCache
import amf.core.client.platform.resource.ResourceLoader
import amf.core.client.platform.transform.TransformationPipeline
import amf.core.internal.convert.ClientErrorHandlerConverter._
import amf.core.internal.convert.TransformationPipelineConverter._
import amf.mcp.client.scala.{MCPConfiguration => InternalMCPConfiguration}
import amf.shapes.client.platform.BaseShapesConfiguration

import scala.concurrent.ExecutionContext
import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class BaseMCPConfiguration private[amf](private[amf] override val _internal: InternalMCPConfiguration)
    extends BaseShapesConfiguration(_internal) {

  override protected implicit val ec: ExecutionContext = _internal.getExecutionContext

  override def withParsingOptions(parsingOptions: ParsingOptions): BaseMCPConfiguration =
    new BaseMCPConfiguration(_internal.withParsingOptions(parsingOptions))

  override def withRenderOptions(renderOptions: RenderOptions): BaseMCPConfiguration =
    new BaseMCPConfiguration(_internal.withRenderOptions(renderOptions))

  override def withErrorHandlerProvider(provider: ErrorHandlerProvider): BaseMCPConfiguration =
    new BaseMCPConfiguration(_internal.withErrorHandlerProvider(() => provider.errorHandler()))

  override def withResourceLoader(rl: ResourceLoader): BaseMCPConfiguration =
    new BaseMCPConfiguration(_internal.withResourceLoader(ResourceLoaderMatcher.asInternal(rl)))

  override def withResourceLoaders(rl: ClientList[ResourceLoader]): BaseMCPConfiguration =
    new BaseMCPConfiguration(_internal.withResourceLoaders(rl.asInternal.toList))

  override def withUnitCache(cache: UnitCache): BaseMCPConfiguration =
    new BaseMCPConfiguration(_internal.withUnitCache(UnitCacheMatcher.asInternal(cache)))

  override def withTransformationPipeline(pipeline: TransformationPipeline): BaseMCPConfiguration =
    new BaseMCPConfiguration(_internal.withTransformationPipeline(pipeline))

  override def withEventListener(listener: AMFEventListener): BaseMCPConfiguration =
    new BaseMCPConfiguration(_internal.withEventListener(listener))

  override def withDialect(dialect: Dialect): BaseMCPConfiguration =
    new BaseMCPConfiguration(_internal.withDialect(dialect))
}
