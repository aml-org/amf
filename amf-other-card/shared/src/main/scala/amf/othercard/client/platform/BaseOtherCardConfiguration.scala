package amf.othercard.client.platform

import amf.aml.client.platform.model.document.Dialect
import amf.aml.internal.convert.VocabulariesClientConverter.{ClientList, _}
import amf.core.client.platform.config.{AMFEventListener, ParsingOptions, RenderOptions}
import amf.core.client.platform.errorhandling.ErrorHandlerProvider
import amf.core.client.platform.reference.UnitCache
import amf.core.client.platform.resource.ResourceLoader
import amf.core.client.platform.transform.TransformationPipeline
import amf.core.internal.convert.ClientErrorHandlerConverter._
import amf.core.internal.convert.TransformationPipelineConverter._
import amf.othercard.client.scala.{OtherCardConfiguration => InternalOtherCardConfiguration}
import amf.shapes.client.platform.config.BaseJsonSchemaBasedSpecConfiguration

import scala.concurrent.ExecutionContext
import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class BaseOtherCardConfiguration private[amf](private[amf] override val _internal: InternalOtherCardConfiguration)
    extends BaseJsonSchemaBasedSpecConfiguration(_internal) {

  override protected implicit val ec: ExecutionContext = _internal.getExecutionContext

  override def withParsingOptions(parsingOptions: ParsingOptions): BaseOtherCardConfiguration =
    new BaseOtherCardConfiguration(_internal.withParsingOptions(parsingOptions))

  override def withRenderOptions(renderOptions: RenderOptions): BaseOtherCardConfiguration =
    new BaseOtherCardConfiguration(_internal.withRenderOptions(renderOptions))

  override def withErrorHandlerProvider(provider: ErrorHandlerProvider): BaseOtherCardConfiguration =
    new BaseOtherCardConfiguration(_internal.withErrorHandlerProvider(() => provider.errorHandler()))

  override def withResourceLoader(rl: ResourceLoader): BaseOtherCardConfiguration =
    new BaseOtherCardConfiguration(_internal.withResourceLoader(ResourceLoaderMatcher.asInternal(rl)))

  override def withResourceLoaders(rl: ClientList[ResourceLoader]): BaseOtherCardConfiguration =
    new BaseOtherCardConfiguration(_internal.withResourceLoaders(rl.asInternal.toList))

  override def withUnitCache(cache: UnitCache): BaseOtherCardConfiguration =
    new BaseOtherCardConfiguration(_internal.withUnitCache(UnitCacheMatcher.asInternal(cache)))

  override def withTransformationPipeline(pipeline: TransformationPipeline): BaseOtherCardConfiguration =
    new BaseOtherCardConfiguration(_internal.withTransformationPipeline(pipeline))

  override def withEventListener(listener: AMFEventListener): BaseOtherCardConfiguration =
    new BaseOtherCardConfiguration(_internal.withEventListener(listener))

  override def withDialect(dialect: Dialect): BaseOtherCardConfiguration =
    new BaseOtherCardConfiguration(_internal.withDialect(dialect))
}
