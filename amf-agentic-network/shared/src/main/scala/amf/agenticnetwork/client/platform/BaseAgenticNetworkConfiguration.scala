package amf.agenticnetwork.client.platform

import amf.aml.client.platform.model.document.Dialect
import amf.aml.internal.convert.VocabulariesClientConverter.{ClientList, _}
import amf.core.client.platform.config.{AMFEventListener, ParsingOptions, RenderOptions}
import amf.core.client.platform.errorhandling.ErrorHandlerProvider
import amf.core.client.platform.reference.UnitCache
import amf.core.client.platform.resource.ResourceLoader
import amf.core.client.platform.transform.TransformationPipeline
import amf.core.internal.convert.ClientErrorHandlerConverter._
import amf.core.internal.convert.TransformationPipelineConverter._
import amf.agenticnetwork.client.scala.{AgenticNetworkConfiguration => InternalAgenticNetworkConfiguration}
import amf.shapes.client.platform.config.BaseJsonSchemaBasedSpecConfiguration

import scala.concurrent.ExecutionContext
import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class BaseAgenticNetworkConfiguration private[amf](private[amf] override val _internal: InternalAgenticNetworkConfiguration)
    extends BaseJsonSchemaBasedSpecConfiguration(_internal) {

  override protected implicit val ec: ExecutionContext = _internal.getExecutionContext

  override def withParsingOptions(parsingOptions: ParsingOptions): BaseAgenticNetworkConfiguration =
    new BaseAgenticNetworkConfiguration(_internal.withParsingOptions(parsingOptions))

  override def withRenderOptions(renderOptions: RenderOptions): BaseAgenticNetworkConfiguration =
    new BaseAgenticNetworkConfiguration(_internal.withRenderOptions(renderOptions))

  override def withErrorHandlerProvider(provider: ErrorHandlerProvider): BaseAgenticNetworkConfiguration =
    new BaseAgenticNetworkConfiguration(_internal.withErrorHandlerProvider(() => provider.errorHandler()))

  override def withResourceLoader(rl: ResourceLoader): BaseAgenticNetworkConfiguration =
    new BaseAgenticNetworkConfiguration(_internal.withResourceLoader(ResourceLoaderMatcher.asInternal(rl)))

  override def withResourceLoaders(rl: ClientList[ResourceLoader]): BaseAgenticNetworkConfiguration =
    new BaseAgenticNetworkConfiguration(_internal.withResourceLoaders(rl.asInternal.toList))

  override def withUnitCache(cache: UnitCache): BaseAgenticNetworkConfiguration =
    new BaseAgenticNetworkConfiguration(_internal.withUnitCache(UnitCacheMatcher.asInternal(cache)))

  override def withTransformationPipeline(pipeline: TransformationPipeline): BaseAgenticNetworkConfiguration =
    new BaseAgenticNetworkConfiguration(_internal.withTransformationPipeline(pipeline))

  override def withEventListener(listener: AMFEventListener): BaseAgenticNetworkConfiguration =
    new BaseAgenticNetworkConfiguration(_internal.withEventListener(listener))

  override def withDialect(dialect: Dialect): BaseAgenticNetworkConfiguration =
    new BaseAgenticNetworkConfiguration(_internal.withDialect(dialect))
}
