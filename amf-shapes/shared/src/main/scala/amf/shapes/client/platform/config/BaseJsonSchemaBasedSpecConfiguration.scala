package amf.shapes.client.platform.config

import amf.aml.client.platform.model.document.Dialect
import amf.aml.internal.convert.VocabulariesClientConverter.{ClientList, _}
import amf.core.client.platform.config.{AMFEventListener, ParsingOptions, RenderOptions}
import amf.core.client.platform.errorhandling.ErrorHandlerProvider
import amf.core.client.platform.reference.UnitCache
import amf.core.client.platform.resource.ResourceLoader
import amf.core.client.platform.transform.TransformationPipeline
import amf.core.internal.convert.ClientErrorHandlerConverter._
import amf.core.internal.convert.TransformationPipelineConverter._
import amf.shapes.client.platform.BaseShapesConfiguration
import amf.shapes.client.scala.{JsonSchemaBasedSpecConfiguration => InternalJsonSchemaBasedSpecConfiguration}

import scala.concurrent.ExecutionContext
import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class BaseJsonSchemaBasedSpecConfiguration private[amf] (
    private[amf] override val _internal: InternalJsonSchemaBasedSpecConfiguration
) extends BaseShapesConfiguration(_internal) {

  override protected implicit val ec: ExecutionContext = _internal.getExecutionContext

  override def withParsingOptions(parsingOptions: ParsingOptions): BaseJsonSchemaBasedSpecConfiguration =
    new BaseJsonSchemaBasedSpecConfiguration(_internal.withParsingOptions(parsingOptions))

  override def withRenderOptions(renderOptions: RenderOptions): BaseJsonSchemaBasedSpecConfiguration =
    new BaseJsonSchemaBasedSpecConfiguration(_internal.withRenderOptions(renderOptions))

  override def withErrorHandlerProvider(provider: ErrorHandlerProvider): BaseJsonSchemaBasedSpecConfiguration =
    new BaseJsonSchemaBasedSpecConfiguration(_internal.withErrorHandlerProvider(() => provider.errorHandler()))

  override def withResourceLoader(rl: ResourceLoader): BaseJsonSchemaBasedSpecConfiguration =
    new BaseJsonSchemaBasedSpecConfiguration(_internal.withResourceLoader(ResourceLoaderMatcher.asInternal(rl)))

  override def withResourceLoaders(rl: ClientList[ResourceLoader]): BaseJsonSchemaBasedSpecConfiguration =
    new BaseJsonSchemaBasedSpecConfiguration(_internal.withResourceLoaders(rl.asInternal.toList))

  override def withUnitCache(cache: UnitCache): BaseJsonSchemaBasedSpecConfiguration =
    new BaseJsonSchemaBasedSpecConfiguration(_internal.withUnitCache(UnitCacheMatcher.asInternal(cache)))

  override def withTransformationPipeline(pipeline: TransformationPipeline): BaseJsonSchemaBasedSpecConfiguration =
    new BaseJsonSchemaBasedSpecConfiguration(_internal.withTransformationPipeline(pipeline))

  override def withEventListener(listener: AMFEventListener): BaseJsonSchemaBasedSpecConfiguration =
    new BaseJsonSchemaBasedSpecConfiguration(_internal.withEventListener(listener))

  override def withDialect(dialect: Dialect): BaseJsonSchemaBasedSpecConfiguration =
    new BaseJsonSchemaBasedSpecConfiguration(_internal.withDialect(dialect))
}
