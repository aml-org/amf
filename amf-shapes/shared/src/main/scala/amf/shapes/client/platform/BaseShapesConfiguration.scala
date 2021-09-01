package amf.shapes.client.platform

import amf.aml.client.platform.BaseAMLConfiguration
import amf.aml.client.platform.model.document.Dialect
import amf.aml.internal.convert.VocabulariesClientConverter.ClientList
import amf.core.client.platform.config.{AMFEventListener, ParsingOptions, RenderOptions}
import amf.core.client.platform.errorhandling.ErrorHandlerProvider
import amf.core.client.platform.reference.UnitCache
import amf.core.client.platform.resource.ResourceLoader
import amf.core.client.platform.transform.TransformationPipeline
import amf.shapes.client.scala.{ShapesConfiguration => InternalShapesConfiguration}
import amf.aml.internal.convert.VocabulariesClientConverter._
import amf.core.internal.convert.ClientErrorHandlerConverter._
import amf.core.internal.convert.TransformationPipelineConverter._

import scala.concurrent.ExecutionContext
import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class BaseShapesConfiguration private[amf] (private[amf] override val _internal: InternalShapesConfiguration)
    extends BaseAMLConfiguration(_internal) {

  override protected implicit val ec: ExecutionContext = _internal.getExecutionContext

  override def withParsingOptions(parsingOptions: ParsingOptions): BaseShapesConfiguration =
    new BaseShapesConfiguration(_internal.withParsingOptions(parsingOptions))

  override def withRenderOptions(renderOptions: RenderOptions): BaseShapesConfiguration =
    new BaseShapesConfiguration(_internal.withRenderOptions(renderOptions))

  override def withErrorHandlerProvider(provider: ErrorHandlerProvider): BaseShapesConfiguration =
    new BaseShapesConfiguration(_internal.withErrorHandlerProvider(() => provider.errorHandler()))

  override def withResourceLoader(rl: ResourceLoader): BaseShapesConfiguration =
    new BaseShapesConfiguration(_internal.withResourceLoader(ResourceLoaderMatcher.asInternal(rl)))

  override def withResourceLoaders(rl: ClientList[ResourceLoader]): BaseShapesConfiguration =
    new BaseShapesConfiguration(_internal.withResourceLoaders(rl.asInternal.toList))

  override def withUnitCache(cache: UnitCache): BaseShapesConfiguration =
    new BaseShapesConfiguration(_internal.withUnitCache(UnitCacheMatcher.asInternal(cache)))

  override def withTransformationPipeline(pipeline: TransformationPipeline): BaseShapesConfiguration =
    new BaseShapesConfiguration(_internal.withTransformationPipeline(pipeline))

  override def withEventListener(listener: AMFEventListener): BaseShapesConfiguration =
    new BaseShapesConfiguration(_internal.withEventListener(listener))

  override def withDialect(dialect: Dialect): BaseShapesConfiguration =
    new BaseShapesConfiguration(_internal.withDialect(dialect))
}
