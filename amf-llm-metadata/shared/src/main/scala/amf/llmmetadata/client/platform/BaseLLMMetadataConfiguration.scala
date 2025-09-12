package amf.llmmetadata.client.platform

import amf.aml.client.platform.model.document.Dialect
import amf.aml.internal.convert.VocabulariesClientConverter.{ClientList, _}
import amf.core.client.platform.config.{AMFEventListener, ParsingOptions, RenderOptions}
import amf.core.client.platform.errorhandling.ErrorHandlerProvider
import amf.core.client.platform.reference.UnitCache
import amf.core.client.platform.resource.ResourceLoader
import amf.core.client.platform.transform.TransformationPipeline
import amf.core.internal.convert.ClientErrorHandlerConverter._
import amf.core.internal.convert.TransformationPipelineConverter._
import amf.llmmetadata.client.scala.{LLMMetadataConfiguration => InternalLLMMetadataConfiguration}
import amf.shapes.client.platform.config.BaseJsonSchemaBasedSpecConfiguration

import scala.concurrent.ExecutionContext
import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class BaseLLMMetadataConfiguration private[amf](private[amf] override val _internal: InternalLLMMetadataConfiguration)
    extends BaseJsonSchemaBasedSpecConfiguration(_internal) {

  override protected implicit val ec: ExecutionContext = _internal.getExecutionContext

  override def withParsingOptions(parsingOptions: ParsingOptions): BaseLLMMetadataConfiguration =
    new BaseLLMMetadataConfiguration(_internal.withParsingOptions(parsingOptions))

  override def withRenderOptions(renderOptions: RenderOptions): BaseLLMMetadataConfiguration =
    new BaseLLMMetadataConfiguration(_internal.withRenderOptions(renderOptions))

  override def withErrorHandlerProvider(provider: ErrorHandlerProvider): BaseLLMMetadataConfiguration =
    new BaseLLMMetadataConfiguration(_internal.withErrorHandlerProvider(() => provider.errorHandler()))

  override def withResourceLoader(rl: ResourceLoader): BaseLLMMetadataConfiguration =
    new BaseLLMMetadataConfiguration(_internal.withResourceLoader(ResourceLoaderMatcher.asInternal(rl)))

  override def withResourceLoaders(rl: ClientList[ResourceLoader]): BaseLLMMetadataConfiguration =
    new BaseLLMMetadataConfiguration(_internal.withResourceLoaders(rl.asInternal.toList))

  override def withUnitCache(cache: UnitCache): BaseLLMMetadataConfiguration =
    new BaseLLMMetadataConfiguration(_internal.withUnitCache(UnitCacheMatcher.asInternal(cache)))

  override def withTransformationPipeline(pipeline: TransformationPipeline): BaseLLMMetadataConfiguration =
    new BaseLLMMetadataConfiguration(_internal.withTransformationPipeline(pipeline))

  override def withEventListener(listener: AMFEventListener): BaseLLMMetadataConfiguration =
    new BaseLLMMetadataConfiguration(_internal.withEventListener(listener))

  override def withDialect(dialect: Dialect): BaseLLMMetadataConfiguration =
    new BaseLLMMetadataConfiguration(_internal.withDialect(dialect))
}
