package amf.brokergroup.client.platform

import amf.aml.client.platform.model.document.Dialect
import amf.aml.internal.convert.VocabulariesClientConverter.{ClientList, _}
import amf.core.client.platform.config.{AMFEventListener, ParsingOptions, RenderOptions}
import amf.core.client.platform.errorhandling.ErrorHandlerProvider
import amf.core.client.platform.reference.UnitCache
import amf.core.client.platform.resource.ResourceLoader
import amf.core.client.platform.transform.TransformationPipeline
import amf.core.internal.convert.ClientErrorHandlerConverter._
import amf.core.internal.convert.TransformationPipelineConverter._
import amf.brokergroup.client.scala.{BrokerGroupConfiguration => InternalBrokerGroupConfiguration}
import amf.shapes.client.platform.config.BaseJsonSchemaBasedSpecConfiguration

import scala.concurrent.ExecutionContext
import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class BaseBrokerGroupConfiguration private[amf](private[amf] override val _internal: InternalBrokerGroupConfiguration)
    extends BaseJsonSchemaBasedSpecConfiguration(_internal) {

  override protected implicit val ec: ExecutionContext = _internal.getExecutionContext

  override def withParsingOptions(parsingOptions: ParsingOptions): BaseBrokerGroupConfiguration =
    new BaseBrokerGroupConfiguration(_internal.withParsingOptions(parsingOptions))

  override def withRenderOptions(renderOptions: RenderOptions): BaseBrokerGroupConfiguration =
    new BaseBrokerGroupConfiguration(_internal.withRenderOptions(renderOptions))

  override def withErrorHandlerProvider(provider: ErrorHandlerProvider): BaseBrokerGroupConfiguration =
    new BaseBrokerGroupConfiguration(_internal.withErrorHandlerProvider(() => provider.errorHandler()))

  override def withResourceLoader(rl: ResourceLoader): BaseBrokerGroupConfiguration =
    new BaseBrokerGroupConfiguration(_internal.withResourceLoader(ResourceLoaderMatcher.asInternal(rl)))

  override def withResourceLoaders(rl: ClientList[ResourceLoader]): BaseBrokerGroupConfiguration =
    new BaseBrokerGroupConfiguration(_internal.withResourceLoaders(rl.asInternal.toList))

  override def withUnitCache(cache: UnitCache): BaseBrokerGroupConfiguration =
    new BaseBrokerGroupConfiguration(_internal.withUnitCache(UnitCacheMatcher.asInternal(cache)))

  override def withTransformationPipeline(pipeline: TransformationPipeline): BaseBrokerGroupConfiguration =
    new BaseBrokerGroupConfiguration(_internal.withTransformationPipeline(pipeline))

  override def withEventListener(listener: AMFEventListener): BaseBrokerGroupConfiguration =
    new BaseBrokerGroupConfiguration(_internal.withEventListener(listener))

  override def withDialect(dialect: Dialect): BaseBrokerGroupConfiguration =
    new BaseBrokerGroupConfiguration(_internal.withDialect(dialect))
}
